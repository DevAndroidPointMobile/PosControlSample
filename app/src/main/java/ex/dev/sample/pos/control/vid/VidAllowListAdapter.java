package ex.dev.sample.pos.control.vid;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ex.dev.sample.pos.control.R;

/**
 * RecyclerView adapter for VID allow list items.
 * Each row shows:
 * - VID text
 * - Delete button to remove the VID from the list
 */
public class VidAllowListAdapter extends RecyclerView.Adapter<VidAllowListAdapter.Holder> {

    /**
     * Callback interface for row actions (delete, etc.).
     * Implemented by VidActivity.
     */
    public interface OnItemActionListener {
        void onDelete(int position);
    }

    // List of VID strings
    private final ArrayList<String> items;
    // Listener for actions
    private final OnItemActionListener listener;
    // Whether row actions are enabled (used when UI is locked/busy)
    private boolean enabled = true;

    public VidAllowListAdapter(ArrayList<String> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /**
     * Enable/disable row interactions
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vid, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        String vid = items.get(position);
        h.tv.setText(vid);

        // Enable/disable delete button
        h.btnDelete.setEnabled(enabled);

        // Bind delete action
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(h.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for each row in the list
     */
    static class Holder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageButton btnDelete;

        Holder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_vid);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}