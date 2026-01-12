package ex.dev.sample.pos.control.cash;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ex.dev.sample.pos.control.R;
import ex.dev.sample.pos.control.data.ApiDataSource;

/**
 * CashActivity
 * <p>
 * This Activity demonstrates a simple POS cash drawer control flow.
 * <p>
 * Responsibilities:
 * 1. Open the cash drawer
 * 2. Query the current open/close status
 * 3. Display the status on screen
 * 4. Prevent multiple simultaneous operations
 * <p>
 * Architecture notes:
 * - UI logic is kept inside the Activity
 * - Hardware/API calls are delegated to ApiDataSource
 * - No background thread is used here (assumes fast / local API)
 */
public class CashActivity extends AppCompatActivity {

    private static final String TAG = "CashActivity";

    /**
     * UI display strings for drawer status
     */
    private static final String STATUS_OPEN = "Status: OPEN";
    private static final String STATUS_CLOSE = "Status: CLOSE";

    /**
     * Abstraction layer for cash drawer hardware or system API
     */
    private final ApiDataSource dataSource = new ApiDataSource();

    // UI elements
    private Button btnOpen;
    private Button btnGetStatus;
    private TextView tvStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity layout
        setContentView(R.layout.activity_cash);

        // Initialize view references
        initViews();

        // Attach click listeners
        bindInteractions();

        // Fetch initial cash drawer status from API
        initializeStatusFromApi();

        /*
         * Handle system window insets (status bar / navigation bar)
         * so the UI is not overlapped on modern edge-to-edge devices.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.root_cash),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );
    }

    /**
     * Find and assign UI components from layout.
     * This method should only contain findViewById calls.
     */
    private void initViews() {
        btnOpen = findViewById(R.id.btn_open_cash);
        btnGetStatus = findViewById(R.id.btn_get_status);
        tvStatus = findViewById(R.id.tv_status);
    }

    /**
     * Bind UI interactions (button click listeners).
     * Keeps onCreate() clean and readable.
     */
    private void bindInteractions() {
        btnOpen.setOnClickListener(v -> onClickOpen());
        btnGetStatus.setOnClickListener(v -> refreshStatusFromApi());
    }

    /**
     * Called once when the Activity starts.
     * <p>
     * Purpose:
     * - Query the current cash drawer status
     * - Update the UI accordingly
     * <p>
     * This ensures the UI reflects the real hardware state
     * instead of assuming a default value.
     */
    private void initializeStatusFromApi() {
        setBusy(true);
        try {
            boolean raw = dataSource.isOpenedCashDrawer();
            updateStatusText(raw);
            Log.d(TAG, "initializeStatusFromApi: raw=" + raw);
        } catch (Throwable t) {
            Log.e(TAG, "initializeStatusFromApi error", t);
            showToast("Init status failed: " + t.getMessage());
        } finally {
            setBusy(false);
        }
    }

    /**
     * Called when the "Get Status" button is pressed.
     * <p>
     * Purpose:
     * - Re-query the cash drawer status from API
     * - Update UI
     * - Provide user feedback via Toast
     */
    private void refreshStatusFromApi() {
        setBusy(true);
        try {
            boolean raw = dataSource.isOpenedCashDrawer();
            updateStatusText(raw);
            showToast(raw ? STATUS_OPEN : STATUS_CLOSE);
            Log.d(TAG, "refreshStatusFromApi: raw=" + raw);
        } catch (Throwable t) {
            Log.e(TAG, "refreshStatusFromApi error", t);
            showToast("Get Status: error - " + t.getMessage());
        } finally {
            setBusy(false);
        }
    }

    /**
     * Called when the "Open" button is pressed.
     * <p>
     * Behavior:
     * - Sends an open command to the cash drawer
     * - Shows result via Toast
     * <p>
     * Design note:
     * - This method intentionally does NOT update the status TextView
     * - Status should always be fetched explicitly via API
     */
    private void onClickOpen() {
        setBusy(true);
        try {
            boolean ok = dataSource.openCashDrawer();
            showToast(ok ? "Open: success" : "Open: failed");
            Log.d(TAG, "openCashDrawer -> " + ok);
        } catch (Throwable t) {
            Log.e(TAG, "openCashDrawer error", t);
            showToast("Open: error - " + t.getMessage());
        } finally {
            setBusy(false);
        }
    }

    /**
     * Update the status TextView based on drawer state.
     *
     * @param isOpen true if drawer is open, false otherwise
     */
    private void updateStatusText(boolean isOpen) {
        tvStatus.setText(isOpen ? STATUS_OPEN : STATUS_CLOSE);
    }

    /**
     * Enable or disable UI controls depending on busy state.
     * <p>
     * When busy:
     * - Buttons are disabled
     * - Prevents multiple simultaneous hardware calls
     */
    private void setBusy(boolean newBusy) {
        /*
         * Flag used to block UI interactions while an operation is in progress.
         * This prevents:
         * - Double clicks
         * - Concurrent hardware calls
         */
        boolean enabled = !newBusy;
        btnOpen.setEnabled(enabled);
        btnGetStatus.setEnabled(enabled);
    }

    /**
     * Display a short Toast message to the user.
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}