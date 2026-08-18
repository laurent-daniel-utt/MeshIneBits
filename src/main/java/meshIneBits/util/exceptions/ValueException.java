package meshIneBits.util.exceptions;

public class ValueException extends RuntimeException {
    public ValueException(String message) {
        super(message, (Throwable) null, false, false);
    }
    public ValueException(String message, String str) {
        super(message.replaceAll("%n", str));
    }
}
