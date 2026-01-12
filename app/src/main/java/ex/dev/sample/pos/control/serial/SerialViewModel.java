package ex.dev.sample.pos.control.serial;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import device.common.SerialPort;
import ex.dev.sample.pos.control.common.ActionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SerialViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    /* ============================
     * UI STATE
     * ============================ */
    private final MutableLiveData<PortUiState> com1State = new MutableLiveData<>(new PortUiState("COM1"));
    private final MutableLiveData<PortUiState> com2State = new MutableLiveData<>(new PortUiState("COM2"));

    public LiveData<PortUiState> getCom1State() {
        return com1State;
    }

    public LiveData<PortUiState> getCom2State() {
        return com2State;
    }

    /* ============================
     * SERIAL CONFIG
     * ============================ */
    private static final int DATA_BITS_8 = 8;
    private static final int BAUDRATE = 115200;
    private static final int FLAGS = DATA_BITS_8; // 8N1

    /* ============================
     * SERIAL PORT
     * ============================ */
    // LPOS / PS72
//    private final SerialPort com1Port = new SerialPort("/dev/ttyXRUSB0");
//    private final SerialPort com2Port = new SerialPort("/dev/ttyXRUSB1");

    /* ============================
     * SERIAL PORT
     * ============================ */
    // PS32
    private final SerialPort com1Port = new SerialPort("/dev/ttyACM0");
    private final SerialPort com2Port = new SerialPort("/dev/ttyACM1");

    /* ============================
     * THREAD
     * ============================ */
    private final ExecutorService ioExecutor =
            Executors.newFixedThreadPool(2);

    private volatile boolean com1Reading = false;
    private volatile boolean com2Reading = false;

    /* ============================
     * TEST BUFFER
     * ============================ */
    private final byte[] testBuffer = new byte[]{
            0x02, 0x02, 0x0B, 0x01, 0x0E, 0x01, 0x57, 0x00,
            0x14, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x32, 0x59, 0x30, 0x30, 0x30, 0x30,
            0x34, 0x32, 0x35, 0x30, 0x30, (byte) 0xFD,
            0x0F, 0x03
    };

    /* ============================
     * OPEN / CLOSE
     * ============================ */
    public ActionResult openCom1Port() {
        try {
            if (!com1Port.isOpened()) {
                com1Port.openPort(BAUDRATE, FLAGS, false);
            }
            com1State.postValue(com1State.getValue()
                    .copy(true, null, null, null));
            startCom1Reader();
            return ActionResult.ok("COM1 opened");

        } catch (Throwable t) {
            Log.e(TAG, "COM1 open failed", t);
            return ActionResult.fail("COM1 open failed: " + t.getMessage());
        }
    }

    public ActionResult openCom2Port() {
        try {
            if (!com2Port.isOpened()) {
                com2Port.openPort(BAUDRATE, FLAGS, false);
            }
            com2State.postValue(com2State.getValue()
                    .copy(true, null, null, null));
            startCom2Reader();
            return ActionResult.ok("COM2 opened");

        } catch (Throwable t) {
            Log.e(TAG, "COM2 open failed", t);
            return ActionResult.fail("COM2 open failed: " + t.getMessage());
        }
    }

    public ActionResult clearCom1Port() {
        com1Reading = false;
        try {
            com1Port.clear();
            com1State.postValue(com1State.getValue()
                    .copy(false, null, null, null));
            return ActionResult.ok("COM1 closed");

        } catch (Throwable t) {
            return ActionResult.fail("COM1 close failed");
        }
    }

    public ActionResult clearCom2Port() {
        com2Reading = false;
        try {
            com2Port.clear();
            com2State.postValue(com2State.getValue()
                    .copy(false, null, null, null));
            return ActionResult.ok("COM2 closed");

        } catch (Throwable t) {
            return ActionResult.fail("COM2 close failed");
        }
    }

    /* ============================
     * WRITE
     * ============================ */
    public ActionResult writeCom1Port() {
        return writeCom1Port(testBuffer);
    }

    public ActionResult writeCom1Port(byte[] buffer) {
        if (!com1Port.isOpened()) {
            return ActionResult.fail("COM1 not opened");
        }
        try {
            com1Port.write(buffer);
            PortUiState prev = com1State.getValue();
            com1State.postValue(
                    prev.copy(null, null,
                            prev.getSentCount() + buffer.length,
                            null)
            );
            return ActionResult.ok("COM1 write " + buffer.length + "B");

        } catch (Throwable t) {
            return ActionResult.fail("COM1 write failed");
        }
    }

    public ActionResult writeCom2Port() {
        return writeCom2Port(testBuffer);
    }

    public ActionResult writeCom2Port(byte[] buffer) {
        if (!com2Port.isOpened()) {
            return ActionResult.fail("COM2 not opened");
        }
        try {
            com2Port.write(buffer);
            PortUiState prev = com2State.getValue();
            com2State.postValue(
                    prev.copy(null, null,
                            prev.getSentCount() + buffer.length,
                            null)
            );
            return ActionResult.ok("COM2 write " + buffer.length + "B");

        } catch (Throwable t) {
            return ActionResult.fail("COM2 write failed");
        }
    }

    /* ============================
     * READ LOOP
     * ============================ */
    private void startCom1Reader() {
        if (com1Reading) return;
        com1Reading = true;

        ioExecutor.execute(() -> readLoop(
                com1Port, com1State, () -> com1Reading));
    }

    private void startCom2Reader() {
        if (com2Reading) return;
        com2Reading = true;

        ioExecutor.execute(() -> readLoop(
                com2Port, com2State, () -> com2Reading));
    }

    private void readLoop(
            SerialPort port,
            MutableLiveData<PortUiState> stateLive,
            ReadingFlag flag
    ) {
        byte[] buf = new byte[4096];
        long backoff = 50;

        while (flag.isRunning() && port.isOpened()) {
            try {
                int len = port.read(buf, buf.length);
                if (len > 0) {
                    backoff = 50;
                    String packet = formatPacket(buf, len);
                    appendPacket(stateLive, packet, len);
                } else {
                    Thread.sleep(10);
                }
            } catch (Throwable t) {
                Log.w(TAG, "read error: " + t.getMessage());
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ignored) {
                }
                backoff = Math.min(backoff * 2, 1000);
            }
        }
    }

    /* ============================
     * PACKET UTIL
     * ============================ */
    private String formatPacket(byte[] buf, int len) {
        int printable = 0;
        for (int i = 0; i < len; i++) {
            byte b = buf[i];
            if (b >= 0x09 && b <= 0x7E) printable++;
        }

        if ((float) printable / len >= 0.85f) {
            return "[SIZE] " + len + "B | " +
                    new String(buf, 0, len);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append(String.format("%02X ", buf[i]));
            }
            return "[SIZE] " + len + "B [DATA] " + sb;
        }
    }

    private void appendPacket(
            MutableLiveData<PortUiState> live,
            String packet,
            int len
    ) {
        PortUiState prev = live.getValue();
        List<String> list = new ArrayList<>();
        list.add(packet);
        list.addAll(prev.getPackets());

        if (list.size() > 200) {
            list = list.subList(0, 200);
        }

        live.postValue(prev.copy(
                null,
                list,
                null,
                prev.getRecvCount() + len
        ));
    }

    /* ============================
     * CLEANUP
     * ============================ */
    @Override
    protected void onCleared() {
        super.onCleared();
        com1Reading = false;
        com2Reading = false;
        ioExecutor.shutdownNow();
        com1Port.clear();
        com2Port.clear();
    }

    /* ============================
     * INTERNAL
     * ============================ */
    private interface ReadingFlag {
        boolean isRunning();
    }
}
