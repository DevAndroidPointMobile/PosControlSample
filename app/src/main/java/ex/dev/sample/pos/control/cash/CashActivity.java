package ex.dev.sample.pos.control.cash;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ex.dev.sample.pos.control.R;
import ex.dev.sample.pos.control.data.ApiDataSource;


/**
 * Cash screen (Java + XML)
 * Responsibilities:
 * - Open: open cash drawer (does NOT change status text)
 * - Get Status: query API and update UI
 * - Initial state: fetch status from API on create
 */
public class CashActivity extends AppCompatActivity {

    // -------------------- constants --------------------
    private static final String TAG = "CashActivity";

    // Status text strings for UI
    private static final String STATUS_OPEN = "Status: OPEN";
    private static final String STATUS_CLOSE = "Status: CLOSE";

    // -------------------- dependencies & state --------------------
    // API data source (wrapper around SDK)
    private final ApiDataSource dataSource = new ApiDataSource();

    // Prevents multiple actions at the same time
    private boolean busy = false;

    // -------------------- views --------------------
    private Button btnOpen;        // "Open" button
    private Button btnGetStatus;   // "Get Status" button
    private TextView tvStatus;     // TextView showing current status

    // -------------------- lifecycle --------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);

        initViews();
        bindInteractions();

        // Fetch initial status from API
        initializeStatusFromApi();
    }

    // -------------------- init / bind --------------------

    /**
     * Initialize view references
     */
    private void initViews() {
        btnOpen = findViewById(R.id.btn_open_cash);
        btnGetStatus = findViewById(R.id.btn_get_status);
        tvStatus = findViewById(R.id.tv_status);
    }

    /**
     * Bind button click listeners
     */
    private void bindInteractions() {
        btnOpen.setOnClickListener(v -> onClickOpen());
        btnGetStatus.setOnClickListener(v -> refreshStatusFromApi());
    }

    // -------------------- actions --------------------

    /**
     * Called on activity start: fetch status from API and update UI
     */
    private void initializeStatusFromApi() {
        setBusy(true);
        try {
            boolean raw = dataSource.isOpenedCashDrawer();
            updateStatusText(raw);
            Log.d(TAG, "init status raw=" + raw);
        } catch (Throwable t) {
            Log.e(TAG, "initializeStatusFromApi error", t);
            showToast("Init status failed: " + t.getMessage());
        } finally {
            setBusy(false);
        }
    }

    /**
     * Called when "Get Status" button is pressed: refresh status from API
     */
    private void refreshStatusFromApi() {
        setBusy(true);
        try {
            boolean raw = dataSource.isOpenedCashDrawer();
            updateStatusText(raw);
            showToast(raw ? STATUS_OPEN : STATUS_CLOSE);
            Log.d(TAG, "refresh status raw=" + raw);
        } catch (Throwable t) {
            Log.e(TAG, "refreshStatusFromApi error", t);
            showToast("Get Status: error - " + t.getMessage());
        } finally {
            setBusy(false);
        }
    }

    /**
     * Called when "Open" button is pressed: open cash drawer (no UI update)
     */
    private void onClickOpen() {
        setBusy(true);
        try {
            boolean ok = dataSource.openCashDrawer();
            showToast(ok ? "Open: success" : "Open: failed");
            Log.d(TAG, "openCashDrawer -> " + ok);
            // NOTE: does not update tvStatus by design
        } catch (Throwable t) {
            Log.e(TAG, "openCashDrawer error", t);
            showToast("Open: error - " + t.getMessage());
        } finally {
            setBusy(false);
        }
    }

    // -------------------- helpers --------------------

    /**
     * Update TextView with OPEN or CLOSE text
     */
    private void updateStatusText(boolean isOpen) {
        tvStatus.setText(isOpen ? STATUS_OPEN : STATUS_CLOSE);
    }

    /**
     * Enable/disable UI controls to prevent multiple actions
     */
    private void setBusy(boolean newBusy) {
        busy = newBusy;
        boolean enabled = !busy;
        btnOpen.setEnabled(enabled);
        btnGetStatus.setEnabled(enabled);
    }

    /**
     * Show short toast message
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}