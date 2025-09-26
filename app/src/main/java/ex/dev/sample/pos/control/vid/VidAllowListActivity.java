package ex.dev.sample.pos.control.vid;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import ex.dev.sample.pos.control.R;
import ex.dev.sample.pos.control.data.ApiDataSource;

/**
 * VidActivity
 * Responsibilities:
 * - Toggle VID allow list enable/disable
 * - Show current allow list from the device
 * - Add/remove VIDs from the list (with validation)
 * - Apply or clear the allow list to the device
 * - Listen for broadcasts of disallowed VIDs
 */
public class VidAllowListActivity extends AppCompatActivity implements VidAllowListAdapter.OnItemActionListener {

    // -------------------- constants --------------------
    public static final String ACTION_DETECTED_DISALLOW_VID = "ACTION_DETECTED_DISALLOW_VID";
    public static final String EXTRA_DISALLOW_VID = "disallow_vid";
    private static final String TAG = "VidActivity";

    // -------------------- broadcast receiver --------------------
    /**
     * Listens for broadcast from SDK/system whenever a disallowed VID is detected.
     * Displays a toast with the blocked VID.
     */
    private final BroadcastReceiver disallowVidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_DETECTED_DISALLOW_VID.equals(intent.getAction())) {
                String vid = intent.getStringExtra(EXTRA_DISALLOW_VID);
                if (vid == null) vid = "(unknown)";
                showToast("[Blocked VID detected] " + vid);
            }
        }
    };

    // -------------------- dependencies & state --------------------
    private final ApiDataSource dataSource = new ApiDataSource();
    private final ArrayList<String> vidList = new ArrayList<>();

    // -------------------- views --------------------
    private SwitchCompat swEnabled;  // switch to enable/disable allow list
    private EditText etVid;          // input field for VID
    private Button btnAdd;           // add VID to list
    private Button btnApply;         // apply VID list to device
    private Button btnClear;         // clear VID list on device
    private RecyclerView rvList;     // recycler view showing VID list
    private VidAllowListAdapter adapter;      // adapter for RecyclerView

    // -------------------- lifecycle --------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_allow_list);

        initViews();
        bindInteractions();
        setupRecycler();

        // Load initial states
        loadEnabled();
        loadListFromDevice();

        // Override back press: simply finish activity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register broadcast receiver for disallowed VIDs
        ContextCompat.registerReceiver(
                this,
                disallowVidReceiver,
                new IntentFilter(ACTION_DETECTED_DISALLOW_VID),
                ContextCompat.RECEIVER_EXPORTED
        );
    }

    @Override
    protected void onPause() {
        unregisterReceiver(disallowVidReceiver);
        super.onPause();
    }

    // -------------------- init / bind --------------------

    /**
     * Find view references
     */
    private void initViews() {
        swEnabled = findViewById(R.id.sw_enable);
        etVid = findViewById(R.id.et_vid);
        btnAdd = findViewById(R.id.btn_add);
        btnApply = findViewById(R.id.btn_apply);
        btnClear = findViewById(R.id.btn_clear);
        rvList = findViewById(R.id.rv_vids);
    }

    /**
     * Bind listeners for switch, buttons, and input
     */
    private void bindInteractions() {
        // Toggle allow list enable/disable
        swEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> onToggleEnable(isChecked));

        // Add button
        btnAdd.setOnClickListener(v -> addFromInput());

        // Keyboard "Done" key triggers add
        etVid.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addFromInput();
                return true;
            }
            return false;
        });

        // Apply button
        btnApply.setOnClickListener(v -> onClickApply());

        // Clear button with confirmation dialog
        btnClear.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear Allow List")
                        .setMessage("Delete all VIDs from the device and the list?")
                        .setPositiveButton("Clear", (d, w) -> performClearAll())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
        );
    }

    /**
     * Setup RecyclerView and adapter
     */
    private void setupRecycler() {
        adapter = new VidAllowListAdapter(vidList, this);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);
        rvList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    // -------------------- actions --------------------

    /**
     * Enable/disable VID allow list
     */
    private void onToggleEnable(boolean isChecked) {
        try {
            dataSource.setAllowListEnabled(isChecked);
            if (isChecked) {
                dataSource.reboot(); // reboot only when enabling
                showToast("Allow List: ON (rebooting to apply)");
            } else {
                showToast("Allow List: OFF");
            }
        } catch (Throwable t) {
            // rollback on failure
            swEnabled.setOnCheckedChangeListener(null);
            swEnabled.setChecked(!isChecked);
            swEnabled.setOnCheckedChangeListener((b, st) -> {
                try {
                    dataSource.setAllowListEnabled(st);
                } catch (Throwable ignore) {
                }
            });
            Log.e(TAG, "setAllowListEnabled error", t);
            showToast("Toggle failed: " + t.getMessage());
        }
    }

    /**
     * Apply current list to device
     */
    private void onClickApply() {
        safeEnable(false);
        try {
            if (vidList.isEmpty()) {
                // clear
                dataSource.clearAllowList();
                dataSource.reboot(); // reboot after clear
                showToast("Cleared on device (empty list), rebooting...");

            } else {
                // set
                String[] arr = vidList.toArray(new String[0]);
                dataSource.setAllowList(arr);
                dataSource.reboot(); // reboot after apply
                showToast("Applied " + arr.length + " item(s), rebooting...");
            }
        } catch (Throwable t) {
            Log.e(TAG, "apply error", t);
            showToast("Apply failed: " + t.getMessage());
        } finally {
            safeEnable(true);
        }
    }

    /**
     * Clear list on device and UI
     */
    @SuppressLint("NotifyDataSetChanged")
    private void performClearAll() {
        safeEnable(false);
        try {
            dataSource.clearAllowList();
            vidList.clear();
            adapter.notifyDataSetChanged();
            dataSource.reboot(); // reboot after clear
            showToast("Cleared, rebooting...");
        } catch (Throwable t) {
            Log.e(TAG, "clear error", t);
            showToast("Clear failed: " + t.getMessage());
        } finally {
            safeEnable(true);
        }
    }

    /**
     * Add new VID from input field
     */
    private void addFromInput() {
        String raw = etVid.getText() != null ? etVid.getText().toString() : "";
        String norm = normalizeVid(raw);
        if (TextUtils.isEmpty(norm)) {
            showToast("Invalid VID. Use 4 or 6 hex digits (e.g., 046D or 0x18D1FF).");
            return;
        }
        if (containsVidByKey(vidList, norm)) {
            showToast("Already exists: " + norm);
            etVid.setText("");
            return;
        }
        vidList.add(norm); // keep original format (with/without 0x)
        adapter.notifyItemInserted(vidList.size() - 1);
        etVid.setText("");
    }

    /**
     * Load allow list enable state from device
     */
    private void loadEnabled() {
        try {
            boolean enabled = dataSource.isVidAllowListEnabled();

            // Temporarily detach listener to prevent firing on programmatic setChecked.
            swEnabled.setOnCheckedChangeListener(null);
            swEnabled.setChecked(enabled);
            swEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> onToggleEnable(isChecked));
            // If you created enableListener field, use: swEnabled.setOnCheckedChangeListener(enableListener);

        } catch (Throwable t) {
            Log.e(TAG, "loadEnabled error", t);
            showToast("Load toggle failed: " + t.getMessage());
        }
    }

    /**
     * Load current VID list from device
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadListFromDevice() {
        try {
            String[] arr = dataSource.getAllowList();
            vidList.clear();
            Collections.addAll(vidList, arr);
            adapter.notifyDataSetChanged();
            Log.d(TAG, "loaded: " + Arrays.toString(vidList.toArray()));

        } catch (
                Throwable t) {
            Log.e(TAG, "loadList error", t);
            showToast("Load list failed: " + t.getMessage());
        }
    }

// -------------------- validation / helpers --------------------

    /**
     * Normalize user input: trim, uppercase, keep 0x prefix if provided
     */
    @Nullable
    private String normalizeVid(@Nullable String input) {
        if (input == null) return null;

        String s = input.trim();
        if (s.isEmpty()) return null;

        if (!s.matches("^(0[xX])?[0-9A-Fa-f]+$")) {
            return null;
        }

        if (s.startsWith("0x") || s.startsWith("0X")) {
            return "0x" + s.substring(2).toUpperCase(Locale.US);
        } else {
            return s.toUpperCase(Locale.US);
        }
    }



    /**
     * Generate canonical key (without prefix) for dedupe checks
     */
    private String vidKey(String vid) {
        if (vid == null) return "";
        String s = vid.trim();
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        return s.toUpperCase(Locale.US);
    }

    /**
     * Check if VID is already present in the list (ignoring prefix differences)
     */
    private boolean containsVidByKey(ArrayList<String> list, String candidate) {
        String key = vidKey(candidate);
        for (String v : list) {
            if (vidKey(v).equals(key)) return true;
        }
        return false;
    }

    /**
     * Enable/disable all inputs while busy
     */
    private void safeEnable(boolean enabled) {
        swEnabled.setEnabled(enabled);
        etVid.setEnabled(enabled);
        btnAdd.setEnabled(enabled);
        btnApply.setEnabled(enabled);
        btnClear.setEnabled(enabled);
        adapter.setEnabled(enabled);
    }

    /**
     * Show a short toast message
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

// -------------------- VidAdapter callback --------------------

    /**
     * Called when delete button in list is pressed
     */
    @Override
    public void onDelete(int position) {
        if (position < 0 || position >= vidList.size()) return;
        String removed = vidList.remove(position);
        adapter.notifyItemRemoved(position);
        showToast("Removed: " + removed);
    }
}