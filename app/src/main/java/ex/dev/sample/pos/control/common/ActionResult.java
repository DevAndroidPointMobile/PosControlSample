package ex.dev.sample.pos.control.common;

import java.util.Objects;

/**
 * ViewModel → UI 결과 전달용 단순 Result 객체
 * <p>
 * - 성공/실패 여부
 * - 사용자에게 보여줄 메시지
 */
public final class ActionResult {

    private final boolean ok;
    private final String message;

    private ActionResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    /* ============================
     * Factory methods
     * ============================ */

    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult fail(String message) {
        return new ActionResult(false, message);
    }

    /* ============================
     * Getter
     * ============================ */

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    /* ============================
     * Object override
     * ============================ */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActionResult)) return false;
        ActionResult that = (ActionResult) o;
        return ok == that.ok &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ok, message);
    }

    @Override
    public String toString() {
        return "ActionResult{" +
                "ok=" + ok +
                ", message='" + message + '\'' +
                '}';
    }
}

