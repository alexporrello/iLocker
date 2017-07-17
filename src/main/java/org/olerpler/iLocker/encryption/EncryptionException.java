package encryption;

public class EncryptionException extends Exception {
	private static final long serialVersionUID = -4451814737551014389L;

	public EncryptionException() {
    }
 
    public EncryptionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
