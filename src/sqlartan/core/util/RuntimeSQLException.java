package sqlartan.core.util;

/**
 * A wrapper Exception class for runtime SQLException.
 */
public class RuntimeSQLException extends RuntimeException {
	public RuntimeSQLException(Throwable cause) {
		super(cause);
	}
}
