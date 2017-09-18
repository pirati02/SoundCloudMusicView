package baqari.dev.com.test;

public class NotSupportedException extends Exception {

    public NotSupportedException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
