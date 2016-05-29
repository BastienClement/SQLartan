package sqlartan.util;

/**
 * An exception wrapped in a RuntimeException.
 */
public class UncheckedException extends RuntimeException {
	/**
	 * @param cause the original exception
	 */
	public UncheckedException(Throwable cause) {
		super(cause);
	}
}
