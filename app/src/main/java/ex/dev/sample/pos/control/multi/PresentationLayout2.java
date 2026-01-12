package ex.dev.sample.pos.control.multi;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import ex.dev.sample.pos.control.R;

public class PresentationLayout2 extends Presentation {

    public PresentationLayout2(Context outerContext, Display display) {
        super(outerContext, display);
    }

    public PresentationLayout2(Context outerContext, Display display, int theme) {
        super(outerContext, display, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_layout_2);
    }
}
