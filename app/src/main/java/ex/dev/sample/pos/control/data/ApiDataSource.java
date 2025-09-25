package ex.dev.sample.pos.control.data;

import android.util.Log;

import device.sdk.Control;
import device.sdk.PosManager;

/**
 * ApiDataSource
 * A wrapper around the SDK (PosManager and Control).
 * <p>
 * Provides clean, app-facing APIs for:
 * - Cash drawer control
 * - VID allow list management
 * - Second display touch control
 * <p>
 * IMPORTANT:
 * - VID allow list operations (set, clear, enable) require a device reboot
 * to take effect. The caller (Activity/UI layer) is responsible for invoking
 * {@link #reboot()} after those operations succeed.
 */
public class ApiDataSource {

    private static final String TAG = "ApiDataSource";

    private static final PosManager manager = PosManager.get();
    private static final Control control = Control.getInstance();

    // --------------------------------------------------------------------------
    // Cash drawer API
    // --------------------------------------------------------------------------

    /**
     * Open the cash drawer.
     */
    public boolean openCashDrawer() {
        return manager.openCashBox();
    }

    /**
     * Check if the cash drawer is currently open.
     */
    public boolean isOpenedCashDrawer() {
        boolean result = manager.getCashBoxStatus();
        Log.d(TAG, "isOpenedCashDrawer: " + result);
        return result;
    }

    // --------------------------------------------------------------------------
    // VID allow list API
    // --------------------------------------------------------------------------

    /**
     * Check if VID allow list feature is enabled.
     */
    public boolean isVidAllowListEnabled() {
        boolean result = manager.isVidAllowListEnabled();
        Log.d(TAG, "isVidAllowListEnabled: " + result);
        return result;
    }

    /**
     * Enable or disable VID allow list feature.
     * NOTE: Caller must reboot the device after enabling/disabling.
     */
    public void setAllowListEnabled(boolean isEnabled) {
        manager.setVidAllowListEnabled(isEnabled);
    }

    /**
     * Get the current VID allow list from the device.
     */
    public String[] getAllowList() {
        String[] result = manager.getVidAllowList();
        Log.d(TAG, "getAllowList: " + (result != null ? java.util.Arrays.toString(result) : "null"));
        return result;
    }

    /**
     * Save a new VID allow list to the device.
     * NOTE: Caller must reboot the device after setting the list.
     *
     * @param vids array of VIDs (must not be null or empty)
     * @throws IllegalArgumentException if vids is null or empty
     */
    public void setAllowList(String[] vids) {
        if (vids == null || vids.length == 0) {
            throw new IllegalArgumentException("vendorIds is null or empty");
        }
        manager.setVidAllowList(vids);
    }

    /**
     * Clear the VID allow list on the device.
     * NOTE: Caller must reboot the device after clearing the list.
     */
    public void clearAllowList() {
        manager.clearVidAllowList();
    }

    // --------------------------------------------------------------------------
    // Second display API
    // --------------------------------------------------------------------------

    /**
     * Get 2nd display touch state.
     */
    public boolean is2ndDisplayTouchEnabled() {
        boolean result = manager.is2ndDisplayTouchEnabled();
        Log.d(TAG, "is2ndDisplayTouchEnabled: " + result);
        return result;
    }

    /**
     * Set 2nd display touch state.
     */
    public void set2ndMonitorTouchEnabled(boolean isEnabled) {
        manager.set2ndDisplayTouchEnabled(isEnabled);
    }

    // --------------------------------------------------------------------------
    // Utility
    // --------------------------------------------------------------------------

    /**
     * Explicit reboot for callers that need it.
     *
     * <p>Important:</p>
     * <ul>
     *   <li>If the first parameter {@code confirm} is set to {@code false},
     *       the device will reboot immediately without showing any system dialog.</li>
     *   <li>If {@code confirm} is set to {@code true}, the system will show
     *       a default confirmation dialog <b>with a fixed message</b>. The message
     *       cannot be customized.</li>
     *   <li>If you need a custom confirmation message or UI, you must implement
     *       your own dialog in the application and call this method with
     *       {@code confirm = false} after user confirmation.</li>
     * </ul>
     */
    public void reboot() {
        control.reboot(true, "APPLY_VID_ALLOW_LIST", false);
    }
}