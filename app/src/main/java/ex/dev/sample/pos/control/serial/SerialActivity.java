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

public class SerialActivity extends AppCompatActivity {

    private SerialViewModel viewModel;
    private PortPanel com1Panel;
    private PortPanel com2Panel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_serial);

        viewModel = new ViewModelProvider(this).get(SerialViewModel.class);

        com1Panel = new PortPanel(findViewById(R.id.panel_com1), "COM1");
        com2Panel = new PortPanel(findViewById(R.id.panel_com2), "COM2");

        bindCom1();
        bindCom2();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void bindCom1() {
        com1Panel.btnOpen.setOnClickListener(v -> show(viewModel.openCom1Port().getMessage()));
        com1Panel.btnClose.setOnClickListener(v -> show(viewModel.closeCom1Port().getMessage()));
        com1Panel.btnStart.setOnClickListener(v -> show(viewModel.startCom1Write().getMessage()));
        com1Panel.btnStop.setOnClickListener(v -> show(viewModel.stopCom1Write().getMessage()));
        com1Panel.btnClear.setOnClickListener(v -> show(viewModel.clearCom1Results().getMessage()));
        viewModel.getCom1State().observe(this, state -> com1Panel.render(state));
    }

    private void bindCom2() {
        com2Panel.btnOpen.setOnClickListener(v -> show(viewModel.openCom2Port().getMessage()));
        com2Panel.btnClose.setOnClickListener(v -> show(viewModel.closeCom2Port().getMessage()));
        com2Panel.btnStart.setOnClickListener(v -> show(viewModel.startCom2Write().getMessage()));
        com2Panel.btnStop.setOnClickListener(v -> show(viewModel.stopCom2Write().getMessage()));
        com2Panel.btnClear.setOnClickListener(v -> show(viewModel.clearCom2Results().getMessage()));
        viewModel.getCom2State().observe(this, state -> com2Panel.render(state));
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}