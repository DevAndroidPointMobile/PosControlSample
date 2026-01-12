package ex.dev.sample.pos.control.multi

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import ex.dev.sample.pos.control.data.ApiDataSource

/**
 * Main Activity that controls multi-display behavior.
 *
 * - Main display (displayId = 0): Jetpack Compose UI
 * - Secondary displays: Android Presentation
 */
class MultiDisplayActivity : AppCompatActivity() {

    /**
     * Data source that maps displayId → unique display identifier (e.g. "local:1")
     * This is usually vendor-specific or device-specific logic.
     */
    private val dataSource = ApiDataSource()

    companion object {

        /**
         * Helper function to create a ComposeView that can be used
         * inside non-Activity environments (e.g. Presentation).
         *
         * Since Presentation is NOT a LifecycleOwner by default,
         * we must manually attach:
         *  - LifecycleOwner
         *  - SavedStateRegistryOwner
         */
        fun makeComposeView(
            context: Context,
            lifecycleOwner: LifecycleOwner
        ): View {
            return ComposeView(context).apply {

                // Attach lifecycle to Compose
                setViewTreeLifecycleOwner(lifecycleOwner)

                // Attach saved-state registry
                setViewTreeSavedStateRegistryOwner(
                    lifecycleOwner as SavedStateRegistryOwner
                )

                setContent {
                    // TODO: Place composable UI here if needed
                }
            }
        }

        /**
         * Logical identifiers for each physical display port.
         * These values are device/manufacturer dependent.
         */
        private const val PORT_HDMI_2 = "local:1"
        private const val PORT_USB_C1 = "local:2"
        private const val PORT_USB_C2 = "local:3"
    }

    /**
     * Currently active Presentation instances.
     * Each external display will have exactly one Presentation.
     */
    private val presentations = mutableListOf<Presentation>()

    /**
     * Listener that reacts to display connection changes.
     *
     * - Added   : HDMI / USB-C plugged in
     * - Removed : Display unplugged
     */
    private val displayListener = object : DisplayManager.DisplayListener {

        override fun onDisplayAdded(displayId: Int) {
            Log.d("MultiDisplay", "Display added: $displayId")
            initPresentations()
        }

        override fun onDisplayRemoved(displayId: Int) {
            Log.d("MultiDisplay", "Display removed: $displayId")
            initPresentations()
        }

        override fun onDisplayChanged(displayId: Int) {
            Log.d("MultiDisplay", "Display changed: $displayId")
        }
    }

    private lateinit var displayManager: DisplayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * Set Compose UI for the MAIN display only.
         * This will always be shown on displayId = 0.
         */
        setContent {
            MainComposeScreen()
        }

        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager

        // Initialize external displays if already connected
        initPresentations()
    }

    override fun onResume() {
        super.onResume()

        // Start listening for display attach/detach events
        displayManager.registerDisplayListener(displayListener, null)
    }

    override fun onPause() {
        super.onPause()

        // Stop listening to avoid memory leaks
        displayManager.unregisterDisplayListener(displayListener)
    }

    /**
     * Creates and shows Presentation objects for all connected
     * secondary displays.
     *
     * This method:
     * 1. Clears existing Presentations
     * 2. Detects all external displays
     * 3. Creates a Presentation per display
     */
    private fun initPresentations() {

        val displays =
            displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)

        // Remove existing Presentations
        presentations.forEach { it.dismiss() }
        presentations.clear()

        for (display in displays) {

            /**
             * Convert displayId → device-specific uniqueId
             * Example: "local:1", "local:2"
             */
            val uniqueId = dataSource.getDisplayUniqueId(display.displayId)

            Log.d("MultiDisplay", "Detected display: $uniqueId")

            /**
             * displayId == 0 is the main screen.
             * It is already used by the Activity, so skip it.
             */
            if (display.displayId == 0) continue

            /**
             * Select Presentation layout based on physical port.
             */
            val presentation = when (uniqueId) {
                PORT_HDMI_2 -> PresentationLayout1(applicationContext, display)
                PORT_USB_C1 -> PresentationLayout2(applicationContext, display)
                PORT_USB_C2 -> PresentationLayout3(applicationContext, display)
                else -> throw IllegalStateException(
                    "Unsupported display port: $uniqueId"
                )
            }

            // Show Presentation on the external display
            presentation.show()
            presentations.add(presentation)
        }
    }
}