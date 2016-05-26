package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.Tokenizable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * An exception thrown if an unexpected token is encountered while
 * processing the input.
 */
public class UnexpectedTokenException extends ParseException {
	/**
	 * The unexpected token
	 */
	public final Token token;

	/**
	 * Constructs an UnexpectedTokenException without a list of expected
	 * tokens.
	 *
	 * @param token the unexpected token
	 */
	public UnexpectedTokenException(Token token) {
		this(token, (Tokenizable[]) null);
	}

	/**
	 * Constructs an UnexpectedTokenException with a list of expected tokens.
	 *
	 * @param token    the unexpected token
	 * @param expected a list of expected tokens at the current location
	 */
	public UnexpectedTokenException(Token token, Tokenizable<?>... expected) {
		super(message(token, expected), token.source, token.offset);
		this.token = token;
	}

	/**
	 * Constructs the error message for an UnexpectedTokenException.
	 *
	 * @param token    the unexpected token
	 * @param expected a list of expected tokens at the current location
	 * @return the message to use for the corresponding exception
	 */
	private static String message(Token token, Tokenizable<?>... expected) {
		String msg = "Unexpected token " + token.toString() + " at offset " + token.offset;
		if (expected != null) {
			Stream<String> expct = Arrays.stream(expected).map(tk -> tk.token().toString());
			msg += " (expected " + String.join(", ", (Iterable<String>) expct::iterator) + ")";
		}
		return msg;
	}
}
