//package ex.dev.sample.pos.control.serial;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import device.common.SerialPort;
//import ex.dev.sample.pos.control.common.ActionResult;
//
//public class PortController {
//
//    private final String label;
//    private final SerialPort port;
//    private final ExecutorService executor = Executors.newSingleThreadExecutor();
//    private final AtomicBoolean running = new AtomicBoolean(false);
//    private final MutableLiveData<PortUiState> state;
//
//    public PortController(String label, String path) {
//        this.label = label;
//        this.port = new SerialPort(path);
//        this.state = new MutableLiveData<>(new PortUiState(label));
//    }
//
//    public LiveData<PortUiState> state() {
//        return state;
//    }
//
//    /* ---------- OPEN / CLOSE ---------- */
//
//    public ActionResult open() {
//        try {
//            port.clear();
//            port.openPort(115200, 8, false);
//            update(s -> s.opened());
//            return ActionResult.ok(label + " opened");
//        } catch (Throwable t) {
//            return ActionResult.fail(label + " open failed");
//        }
//    }
//
//    public ActionResult close() {
//        running.set(false);
//        port.clear();
//        update(s -> s.closed());
//        return ActionResult.ok(label + " closed");
//    }
//
//    /* ---------- START / STOP ---------- */
//
//    public ActionResult start(byte[] testBuffer) {
//        if (!port.isOpened())
//            return ActionResult.fail(label + " not opened");
//
//        if (!running.compareAndSet(false, true))
//            return ActionResult.fail(label + " already running");
//
//        update(PortUiState::reset);
//        executor.execute(() -> ioLoop(testBuffer));
//        return ActionResult.ok(label + " started");
//    }
//
//    public ActionResult stop() {
//        running.set(false);
//        update(s -> s.writing(false));
//        return ActionResult.ok(label + " stopped");
//    }
//
//    public ActionResult clear() {
//        update(PortUiState::clear);
//        return ActionResult.ok(label + " cleared");
//    }
//
//    /* ---------- CORE LOOP ---------- */
//
//    private void ioLoop(byte[] testBuffer) {
//        byte[] buf = new byte[4096];
//
//        while (running.get() && port.isOpened()) {
//            try {
//                port.write(testBuffer);
//                int recv = port.read(buf, 1000);
//
//                boolean pass = recv == testBuffer.length &&
//                        equals(buf, testBuffer, recv);
//
//                update(s -> s.appendResult(pass, testBuffer.length, recv));
//
//                if (pass) Thread.sleep(500);
//            } catch (Throwable ignore) {
//            }
//        }
//    }
//
//    private boolean equals(byte[] a, byte[] b, int len) {
//        for (int i = 0; i < len; i++)
//            if (a[i] != b[i]) return false;
//        return true;
//    }
//
//    private void update(StateReducer reducer) {
//        PortUiState prev = state.getValue();
//        if (prev != null)
//            state.postValue(reducer.reduce(prev));
//    }
//
//    private interface StateReducer {
//        PortUiState reduce(PortUiState prev);
//    }
//
//    public void release() {
//        running.set(false);
//        executor.shutdownNow();
//        port.clear();
//    }
//}
//
