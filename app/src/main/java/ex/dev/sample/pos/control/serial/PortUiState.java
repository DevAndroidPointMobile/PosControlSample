package ex.dev.sample.pos.control.serial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PortUiState {

    private final String label;
    private final boolean isOpened;
    private final List<String> packets;
    private final int sentCount;
    private final int recvCount;

    public PortUiState(String label) {
        this(label, false, Collections.emptyList(), 0, 0);
    }

    public PortUiState(
            String label,
            boolean isOpened,
            List<String> packets,
            int sentCount,
            int recvCount
    ) {
        this.label = label;
        this.isOpened = isOpened;
        this.packets = packets == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(packets));
        this.sentCount = sentCount;
        this.recvCount = recvCount;
    }

    // ───────────────
    // Getter
    // ───────────────
    public String getLabel() {
        return label;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public List<String> getPackets() {
        return packets;
    }

    public int getSentCount() {
        return sentCount;
    }

    public int getRecvCount() {
        return recvCount;
    }

    public PortUiState copy(
            Boolean isOpened,
            List<String> packets,
            Integer sentCount,
            Integer recvCount
    ) {
        return new PortUiState(
                this.label,
                isOpened != null ? isOpened : this.isOpened,
                packets != null ? packets : this.packets,
                sentCount != null ? sentCount : this.sentCount,
                recvCount != null ? recvCount : this.recvCount
        );
    }

    // ───────────────
    // equals / hashCode / toString
    // ───────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortUiState)) return false;
        PortUiState that = (PortUiState) o;
        return isOpened == that.isOpened &&
                sentCount == that.sentCount &&
                recvCount == that.recvCount &&
                Objects.equals(label, that.label) &&
                Objects.equals(packets, that.packets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, isOpened, packets, sentCount, recvCount);
    }

    @Override
    public String toString() {
        return "PortUiState{" +
                "label='" + label + '\'' +
                ", isOpened=" + isOpened +
                ", packets=" + packets.size() +
                ", sentCount=" + sentCount +
                ", recvCount=" + recvCount +
                '}';
    }
}
