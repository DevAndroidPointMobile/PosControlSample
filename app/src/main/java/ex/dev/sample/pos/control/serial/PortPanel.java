package ex.dev.sample.pos.control.serial;

import android.util.Log;
import android.view.View;
import android.widget.*;

import ex.dev.sample.pos.control.R;

import java.util.ArrayList;

public class PortPanel {
    TextView title, status;
    Button btnOpen, btnClose, btnStart, btnStop, btnClear;
    ListView listView;
    ArrayAdapter<String> adapter;

    public PortPanel(View root, String label) {
        title = root.findViewById(R.id.txtTitle);
        status = root.findViewById(R.id.txtStatus);
        btnOpen = root.findViewById(R.id.btnOpen);
        btnClose = root.findViewById(R.id.btnClose);
        btnStart = root.findViewById(R.id.btnStart);
        btnStop = root.findViewById(R.id.btnStop);
        btnClear = root.findViewById(R.id.btnClear);
        listView = root.findViewById(R.id.listPackets);

        title.setText(label + " PORT");

        adapter = new ArrayAdapter<>(root.getContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>());
        listView.setAdapter(adapter);
    }

    public void render(PortUiState state) {
        if (state.isOpened()) {
            status.setText(String.format(
                    "OPEN | Sent: %dB | Recv: %dB | PASS: %d | FAIL: %d",
                    state.getSentCount(),
                    state.getRecvCount(),
                    state.getPassCount(),
                    state.getFailCount()
            ));
        } else {
            status.setText("CLOSED");
        }

        btnOpen.setEnabled(!state.isOpened());
        btnClose.setEnabled(state.isOpened());
        btnStart.setEnabled(state.isOpened() && !state.isWriting());
        btnStop.setEnabled(state.isOpened() && state.isWriting());
        btnClear.setEnabled(state.isOpened());

        adapter.clear();
        adapter.addAll(state.getPackets());
        adapter.notifyDataSetChanged();
    }
}