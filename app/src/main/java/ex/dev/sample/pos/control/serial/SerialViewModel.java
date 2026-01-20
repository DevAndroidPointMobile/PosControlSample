package ex.dev.sample.pos.control.serial;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import device.common.SerialPort;
import ex.dev.sample.pos.control.common.ActionResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialViewModel extends ViewModel {

    private static final String TAG = "SerialViewModel";

    private static final int BAUDRATE = 115200;
    private static final int FLAGS = 8;

    private static final long WRITE_INTERVAL_MS = 500;

    private final SerialPort com1Port = new SerialPort(PortConstants.PATH_PS72_PORT_1);
    private final SerialPort com2Port = new SerialPort(PortConstants.PATH_PS72_PORT_2);

    private final ExecutorService com1Executor = Executors.newSingleThreadExecutor();
    private final ExecutorService com2Executor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean com1Running = new AtomicBoolean(false);
    private final AtomicBoolean com2Running = new AtomicBoolean(false);

    private final MutableLiveData<PortUiState> com1State = new MutableLiveData<>(new PortUiState("COM1"));
    private final MutableLiveData<PortUiState> com2State = new MutableLiveData<>(new PortUiState("COM2"));

    public LiveData<PortUiState> getCom1State() {
        return com1State;
    }

    public LiveData<PortUiState> getCom2State() {
        return com2State;
    }

    private final byte[] testBuffer = new byte[]{
            0x02, 0x02, 0x0B, 0x01, 0x0E, 0x01, 0x57, 0x00,
            0x14, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x32, 0x59, 0x30, 0x30, 0x30, 0x30,
            0x34, 0x32, 0x35, 0x30, 0x30, (byte) 0xFD,
            0x0F, 0x03
    };

    /* ---------------- OPEN / CLOSE ---------------- */

    public ActionResult openCom1Port() {
        try {
            if (com1Port.isOpened())
                com1Port.clear();

            com1Port.openPort(BAUDRATE, FLAGS, false);
            updateState(com1State, s -> s.copy(true, null, null, null, false, null, null));
            return ActionResult.ok("COM1 opened");

        } catch (Throwable t) {
            return ActionResult.fail("COM1 open failed");
        }
    }

    public ActionResult openCom2Port() {
        try {
            if (com2Port.isOpened())
                com2Port.clear();

            com2Port.openPort(BAUDRATE, FLAGS, false);
            updateState(com2State, s -> s.copy(true, null, null, null, false, null, null));
            return ActionResult.ok("COM2 opened");
        } catch (Throwable t) {
            return ActionResult.fail("COM2 open failed");
        }
    }

    public ActionResult closeCom1Port() {
        com1Running.set(false);
        com1Port.clear();
        updateState(com1State, s -> s.copy(false, null, null, null, false, null, null));
        return ActionResult.ok("COM1 closed");
    }

    public ActionResult closeCom2Port() {
        com2Running.set(false);
        com2Port.clear();
        updateState(com2State, s -> s.copy(false, null, null, null, false, null, null));
        return ActionResult.ok("COM2 closed");
    }

    /* ---------------- START / STOP ---------------- */

    public ActionResult startCom1Write() {
        if (!com1Port.isOpened()) return ActionResult.fail("COM1 not opened");
        if (!com1Running.compareAndSet(false, true)) {
            return ActionResult.fail("COM1 already running");
        }

        resetState(com1State, "COM1");
        com1Executor.execute(() -> ioLoop(com1Port, com1State, com1Running));
        return ActionResult.ok("COM1 started");
    }

    public ActionResult startCom2Write() {
        if (!com2Port.isOpened()) return ActionResult.fail("COM2 not opened");
        if (!com2Running.compareAndSet(false, true)) {
            return ActionResult.fail("COM2 already running");
        }

        resetState(com2State, "COM2");
        com2Executor.execute(() ->
                ioLoop(com2Port, com2State, com2Running));
        return ActionResult.ok("COM2 started");
    }

    public ActionResult stopCom1Write() {
        com1Running.set(false);
        updateState(com1State, s -> s.copy(null, null, null, null, false, null, null));
        return ActionResult.ok("COM1 stopped");
    }

    public ActionResult stopCom2Write() {
        com2Running.set(false);
        updateState(com2State, s -> s.copy(null, null, null, null, false, null, null));
        return ActionResult.ok("COM2 stopped");
    }

    /* ---------------- CLEAR ---------------- */

    public ActionResult clearCom1Results() {
        updateState(com1State, s -> s.copy(null, new ArrayList<>(), 0, 0, null, 0, 0));
        return ActionResult.ok("COM2 cleared");
    }

    public ActionResult clearCom2Results() {
        updateState(com2State, s -> s.copy(null, new ArrayList<>(), 0, 0, null, 0, 0));
        return ActionResult.ok("COM2 cleared");
    }

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private String nowTimestamp() {
        return LocalDateTime.now().format(TIME_FMT);
    }

    /* ---------------- CORE LOOP ---------------- */

    private void ioLoop(
            SerialPort port,
            MutableLiveData<PortUiState> live,
            AtomicBoolean running
    ) {
        byte[] buf = new byte[4096];

        while (running.get() && port.isOpened()) {
            try {
                // 1. WRITE
                port.write(testBuffer);
                int sentBytes = testBuffer.length;

                // 2. READ (blocking with internal timeout)
                int recvLen = port.read(buf, 1000);

                boolean received = recvLen > 0;
                boolean matched = false;

                if (received && recvLen == testBuffer.length) {
                    matched = true;
                    for (int i = 0; i < recvLen; i++) {
                        if (buf[i] != testBuffer[i]) {
                            matched = false;
                            break;
                        }
                    }
                }

                boolean pass = received && matched;

                // 3. UPDATE STATE
                PortUiState prev = live.getValue();
                if (prev != null) {
                    List<String> packets = new ArrayList<>(prev.getPackets());

                    if (pass) {
                        packets.add(0, "[PASS] SUCCESS " + nowTimestamp());
                    } else if (!received) {
                        packets.add(0, "[FAIL] RESPONSE TIMEOUT");
                    } else {
                        packets.add(0, "[FAIL] DATA MISMATCH");
                    }

                    if (packets.size() > 200) {
                        packets = packets.subList(0, 200);
                    }

                    live.postValue(new PortUiState(
                            prev.getLabel(),
                            true,
                            packets,
                            prev.getSentCount() + sentBytes,
                            prev.getRecvCount() + Math.max(recvLen, 0),
                            true,
                            prev.getPassCount() + (pass ? 1 : 0),
                            prev.getFailCount() + (pass ? 0 : 1)
                    ));
                }

                // PASS일 때만 주기 유지
                if (pass) {
                    Thread.sleep(WRITE_INTERVAL_MS);
                }

            } catch (Throwable t) {
                Log.e(TAG, "IO loop error", t);
            }
        }
    }


    /* ---------------- HELPERS ---------------- */

    private void resetState(MutableLiveData<PortUiState> live, String label) {
        live.postValue(new PortUiState(label, true,
                new ArrayList<>(), 0, 0, true, 0, 0));
    }

    private void updateState(
            MutableLiveData<PortUiState> live,
            StateUpdater updater
    ) {
        PortUiState prev = live.getValue();
        if (prev != null) live.postValue(updater.update(prev));
    }

    private interface StateUpdater {
        PortUiState update(PortUiState prev);
    }

    private String formatPacket(byte[] buf, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X ", buf[i]));
        }
        return "[SIZE] " + len + "B [DATA] " + sb;
    }

    @Override
    protected void onCleared() {
        com1Running.set(false);
        com2Running.set(false);
        com1Executor.shutdownNow();
        com2Executor.shutdownNow();
        com1Port.clear();
        com2Port.clear();
    }
}
