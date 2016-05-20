package sqlartan.core.util;

/**
 * A wrapper Exception class for runtime SQLException.
 */
public class UncheckedSQLException extends RuntimeException {
	public UncheckedSQLException(Throwable cause) {
		super(cause);
	}
}
