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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ex.dev.sample.pos.control.R;
import ex.dev.sample.pos.control.data.ApiDataSource;

/**
 * SecondDisplayControlActivity
 * <p>
 * Controls touch input availability on the second (external) display.
 * <p>
 * Responsibilities:
 * - Enable / disable touch input for the 2nd display
 * - Fetch current state from API
 * - Perform optimistic UI updates
 * - Roll back UI state on failure
 * <p>
 * Typical use case:
 * - POS systems where the customer display may need to be
 * temporarily locked or unlocked.
 */
public class SecondDisplayControlActivity extends AppCompatActivity {

    private static final String TAG = "SecondDisplayControlActivity";

    /**
     * Data source responsible for communicating with
     * system / hardware / vendor APIs.
     */
    private final ApiDataSource dataSource = new ApiDataSource();

    /**
     * Flag used to prevent multiple concurrent operations.
     */
    private boolean isBusy = false;

    // UI components
    private SwitchCompat swTouchEnabled;
    private Button btnRefresh;
    private TextView tvStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity layout
        setContentView(R.layout.activity_second_display_control);

        // Initialize UI references
        initViews();

        // Bind user interactions
        bindInteractions();

        // Load initial state from API
        loadState();

        /*
         * Apply system window insets (status bar / navigation bar)
         * to support edge-to-edge layouts.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.root_second_display),
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
     * Initialize all view references from XML layout.
     */
    private void initViews() {
        swTouchEnabled = findViewById(R.id.sw_touch_enabled);
        btnRefresh = findViewById(R.id.btn_refresh);
        tvStatus = findViewById(R.id.tv_status_2nd);
    }

    /**
     * Bind UI interactions.
     * <p>
     * - Switch toggle → optimistic update
     * - Refresh button → explicit state reload from API
     */
    private void bindInteractions() {

        // Optimistic update when user toggles the switch
        swTouchEnabled.setOnCheckedChangeListener(this::onToggle);

        // Explicit refresh from API (source of truth)
        btnRefresh.setOnClickListener(v -> loadState());
    }

    /**
     * Called when the user toggles the touch enable switch.
     * <p>
     * Behavior:
     * - Immediately applies the change via API
     * - Updates UI optimistically
     * - Rolls back switch state if API call fails
     */
    private void onToggle(CompoundButton buttonView, boolean newChecked) {
        if (isBusy) return;

        try {
            setBusy(true);

            // Apply new state to the system
            dataSource.set2ndMonitorTouchEnabled(newChecked);

            // Optimistically update UI
            updateStatusText(newChecked);
            showToast("2nd Touch: " + (newChecked ? "ENABLED" : "DISABLED"));

        } catch (Throwable t) {
            Log.e(TAG, "set2ndMonitorTouchEnabled error", t);
            showToast("Failed to change: " + t.getMessage());

            // Roll back UI to previous state
            rollbackSwitch(newChecked);
        } finally {
            setBusy(false);
        }
    }

    /**
     * Loads the current touch enable state from API
     * and updates the UI accordingly.
     * <p>
     * This method is the single source of truth.
     */
    private void loadState() {
        if (isBusy) return;

        setBusy(true);
        try {
            boolean enabled = dataSource.is2ndDisplayTouchEnabled();

            /*
             * Temporarily remove the listener to avoid triggering
             * onToggle() when updating the switch programmatically.
             */
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

    /**
     * Roll back the switch to the previous value
     * after a failed API operation.
     */
    private void rollbackSwitch(boolean attemptedValue) {
        swTouchEnabled.setOnCheckedChangeListener(null);
        swTouchEnabled.setChecked(!attemptedValue);
        swTouchEnabled.setOnCheckedChangeListener(this::onToggle);
    }

    /**
     * Update the status label based on current state.
     */
    private void updateStatusText(boolean enabled) {
        tvStatus.setText(
                enabled
                        ? "Status: TOUCH ENABLED"
                        : "Status: TOUCH DISABLED"
        );
    }

    /**
     * Enable or disable UI controls based on busy state.
     * <p>
     * When busy:
     * - Switch and button are disabled
     * - Prevents concurrent API calls
     */
    private void setBusy(boolean newBusy) {
        isBusy = newBusy;
        boolean enabled = !isBusy;
        swTouchEnabled.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
    }

    /**
     * Display a short Toast message.
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
