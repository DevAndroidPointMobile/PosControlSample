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
    private final boolean isWriting;
    private final int passCount;
    private final int failCount;

    public PortUiState(String label) {
        this(label, false, Collections.emptyList(), 0, 0, false, 0, 0);
    }

    public PortUiState(
            String label,
            boolean isOpened,
            List<String> packets,
            int sentCount,
            int recvCount,
            boolean isWriting,
            int passCount,
            int failCount
    ) {
        this.label = label;
        this.isOpened = isOpened;
        this.packets = packets == null
                ? Collections.emptyList()
                : List.copyOf(packets);
        this.sentCount = sentCount;
        this.recvCount = recvCount;
        this.isWriting = isWriting;
        this.passCount = passCount;
        this.failCount = failCount;
    }

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

    public boolean isWriting() {
        return isWriting;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    /**
     * Create a copy with updated fields. Pass null to keep existing value.
     */
    public PortUiState copy(
            Boolean isOpened,
            List<String> packets,
            Integer sentCount,
            Integer recvCount,
            Boolean isWriting,
            Integer passCount,
            Integer failCount
    ) {
        return new PortUiState(
                this.label,
                isOpened != null ? isOpened : this.isOpened,
                packets != null ? packets : this.packets,
                sentCount != null ? sentCount : this.sentCount,
                recvCount != null ? recvCount : this.recvCount,
                isWriting != null ? isWriting : this.isWriting,
                passCount != null ? passCount : this.passCount,
                failCount != null ? failCount : this.failCount
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortUiState)) return false;
        PortUiState that = (PortUiState) o;
        return isOpened == that.isOpened &&
                sentCount == that.sentCount &&
                recvCount == that.recvCount &&
                isWriting == that.isWriting &&
                passCount == that.passCount &&
                failCount == that.failCount &&
                Objects.equals(label, that.label) &&
                Objects.equals(packets, that.packets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, isOpened, packets, sentCount, recvCount, isWriting, passCount, failCount);
    }

    @Override
    public String toString() {
        return "PortUiState{" +
                "label='" + label + '\'' +
                ", isOpened=" + isOpened +
                ", packets=" + packets.size() +
                ", sentCount=" + sentCount +
                ", recvCount=" + recvCount +
                ", isWriting=" + isWriting +
                ", passCount=" + passCount +
                ", failCount=" + failCount +
                '}';
    }
}