package ex.dev.sample.pos.control.serial;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import ex.dev.sample.pos.control.R;

/**
 * SerialActivity
 * <p>
 * UI entry point for serial port testing.
 * <p>
 * - Displays two independent port panels (COM1 / COM2)
 * - Delegates all business logic to SerialViewModel
 * - Observes port state and renders UI accordingly
 */
public class SerialActivity extends AppCompatActivity {

    private SerialViewModel viewModel;

    // UI wrappers for each serial port
    private PortPanel com1Panel;
    private PortPanel com2Panel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge layout (Android 13+ style)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_serial);

        // Obtain ViewModel scoped to this Activity
        viewModel = new ViewModelProvider(this).get(SerialViewModel.class);

        // Initialize UI panels for each port
        com1Panel = new PortPanel(findViewById(R.id.panel_com1), "COM1");
        com2Panel = new PortPanel(findViewById(R.id.panel_com2), "COM2");

        // Bind UI events and state observers
        bindCom1();
        bindCom2();

        // Apply system bar insets to root layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });
    }

    /**
     * Bind COM1 UI actions and state observer.
     */
    private void bindCom1() {
        com1Panel.btnOpen.setOnClickListener(
                v -> show(viewModel.openCom1Port().getMessage())
        );

        com1Panel.btnClose.setOnClickListener(
                v -> show(viewModel.clearCom1Port().getMessage())
        );

        com1Panel.btnWrite.setOnClickListener(
                v -> show(viewModel.writeCom1Port().getMessage())
        );

        // Observe COM1 state changes and re-render panel
        viewModel.getCom1State().observe(
                this,
                state -> com1Panel.render(state)
        );
    }

    /**
     * Bind COM2 UI actions and state observer.
     */
    private void bindCom2() {
        com2Panel.btnOpen.setOnClickListener(
                v -> show(viewModel.openCom2Port().getMessage())
        );

        com2Panel.btnClose.setOnClickListener(
                v -> show(viewModel.clearCom2Port().getMessage())
        );

        com2Panel.btnWrite.setOnClickListener(
                v -> show(viewModel.writeCom2Port().getMessage())
        );

        // Observe COM2 state changes and re-render panel
        viewModel.getCom2State().observe(
                this,
                state -> com2Panel.render(state)
        );
    }

    /**
     * Display a short toast message.
     */
    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
