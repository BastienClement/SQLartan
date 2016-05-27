package sqlartan.core.ast.token;

import sqlartan.core.ast.parser.ParseException;

/**
 * An exception occurring during the tokenization phase.
 */
public class TokenizeException extends ParseException {
	/**
	 * @param message the exception message
	 * @param source  the SQL source being tokenized
	 * @param offset  the offset in the SQL source where the error occurred
	 */
	public TokenizeException(String message, String source, int offset) {
		super(message, source, offset);
	}
}
