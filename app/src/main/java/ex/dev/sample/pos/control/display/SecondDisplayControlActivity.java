package ex.dev.sample.pos.control.display;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import ex.dev.sample.pos.control.R;
import ex.dev.sample.pos.control.data.ApiDataSource;

/**
 * Second Display screen
 * Responsibilities:
 * - Toggle 2nd display touch enable/disable
 * - Refresh current state from API
 * - Optimistic UI update with rollback on failure
 */
public class SecondDisplayControlActivity extends AppCompatActivity {

    // -------------------- constants --------------------
    private static final String TAG = "SecondDisplayControlActivity";

    // -------------------- dependencies & state --------------------
    private final ApiDataSource dataSource = new ApiDataSource();
    private boolean isBusy = false;

    // -------------------- views --------------------
    private SwitchCompat swTouchEnabled;   // toggle for 2nd display touch
    private Button btnRefresh;             // refresh button
    private TextView tvStatus;             // status label

    // -------------------- lifecycle --------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_display_control);

        initViews();
        bindInteractions();

        // Fetch initial state from API
        loadState();
    }

    // -------------------- init / bind --------------------

    /**
     * Initialize view references
     */
    private void initViews() {
        swTouchEnabled = findViewById(R.id.sw_touch_enabled);
        btnRefresh = findViewById(R.id.btn_refresh);
        tvStatus = findViewById(R.id.tv_status_2nd);
    }

    /**
     * Bind button and switch listeners
     */
    private void bindInteractions() {
        // Optimistic update on switch toggle
        swTouchEnabled.setOnCheckedChangeListener(this::onToggle);

        // Refresh button explicitly re-queries API
        btnRefresh.setOnClickListener(v -> loadState());
    }

    // -------------------- actions --------------------

    /**
     * Called when user toggles the switch
     */
    private void onToggle(CompoundButton buttonView, boolean newChecked) {
        if (isBusy) return;

        try {
            setBusy(true);
            dataSource.set2ndMonitorTouchEnabled(newChecked);
            updateStatusText(newChecked);
            showToast("2nd Touch: " + (newChecked ? "ENABLED" : "DISABLED"));

        } catch (Throwable t) {
            Log.e(TAG, "set2ndMonitorTouchEnabled error", t);
            showToast("Failed to change: " + t.getMessage());

            rollbackSwitch(newChecked);
        } finally {
            setBusy(false);
        }
    }

    /**
     * Load current state from API and update UI
     */
    private void loadState() {
        if (isBusy) return;
        setBusy(true);
        try {
            boolean enabled = dataSource.is2ndDisplayTouchEnabled();

            // Avoid triggering listener while programmatically setting value
            swTouchEnabled.setOnCheckedChangeListener(null);
            swTouchEnabled.setChecked(enabled);
            swTouchEnabled.setOnCheckedChangeListener(this::onToggle);

            updateStatusText(enabled);
        } catch (Throwable t) {
            Log.e(TAG, "loadState error", t);
            showToast("Load failed: " + t.getMessage());
        } finally {
            setBusy(false);
        }
    }

    // -------------------- helpers --------------------

    /**
     * Roll back the switch state after a failed toggle
     */
    private void rollbackSwitch(boolean attemptedValue) {
        swTouchEnabled.setOnCheckedChangeListener(null);
        swTouchEnabled.setChecked(!attemptedValue);
        swTouchEnabled.setOnCheckedChangeListener(this::onToggle);
    }

    /**
     * Update status label
     */
    private void updateStatusText(boolean enabled) {
        tvStatus.setText(enabled ? "Status: TOUCH ENABLED" : "Status: TOUCH DISABLED");
    }

    /**
     * Set busy state and disable controls during operations
     */
    private void setBusy(boolean newBusy) {
        isBusy = newBusy;
        boolean enabled = !isBusy;
        swTouchEnabled.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
    }

    /**
     * Show a short toast
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}