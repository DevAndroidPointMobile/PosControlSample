package ex.dev.sample.pos.control.multi;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import ex.dev.sample.pos.control.R;

/**
 * Presentation shown on a specific external display (e.g. HDMI).
 * <p>
 * Notes:
 * - Presentation runs on a DIFFERENT display than the Activity
 * - It has its own Window and lifecycle
 * - Usually used for POS customer screens, signage, etc.
 */
public class PresentationLayout1 extends Presentation {

    /**
     * Constructor used when no custom theme is needed.
     */
    public PresentationLayout1(Context outerContext, Display display) {
        super(outerContext, display);
    }

    /**
     * Constructor with explicit theme support.
     */
    public PresentationLayout1(Context outerContext, Display display, int theme) {
        super(outerContext, display, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Inflate XML layout for this external display.
         * This layout will ONLY appear on the assigned display.
         */
        setContentView(R.layout.presentation_layout_1);
    }
}
