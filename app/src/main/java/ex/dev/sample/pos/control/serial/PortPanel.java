package ex.dev.sample.pos.control.serial;

import android.view.View;
import android.widget.*;

import ex.dev.sample.pos.control.R;

import java.util.ArrayList;

public class PortPanel {

    TextView title, status;
    Button btnOpen, btnClose, btnWrite;
    ListView listView;
    ArrayAdapter<String> adapter;

    public PortPanel(View root, String label) {
        title = root.findViewById(R.id.txtTitle);
        status = root.findViewById(R.id.txtStatus);
        btnOpen = root.findViewById(R.id.btnOpen);
        btnClose = root.findViewById(R.id.btnClose);
        btnWrite = root.findViewById(R.id.btnWrite);
        listView = root.findViewById(R.id.listPackets);

        title.setText(label + " PORT");

        adapter = new ArrayAdapter<>(root.getContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>());
        listView.setAdapter(adapter);
    }

    public void render(PortUiState state) {
        if (state.isOpened()) {
            status.setText("OPEN | Sent: " + state.getSentCount() + "B Recv: " + state.getRecvCount() + "B");
        } else {
            status.setText("CLOSED");
        }

        btnOpen.setEnabled(!state.isOpened());
        btnClose.setEnabled(state.isOpened());
        btnWrite.setEnabled(state.isOpened());

        adapter.clear();
        adapter.addAll(state.getPackets());
        adapter.notifyDataSetChanged();
    }
}
