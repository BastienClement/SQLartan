package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.Tokenizable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An unexpected token occurred while parsing the input
 */
@SuppressWarnings("WeakerAccess")
public class UnexpectedTokenException extends ParseException {
	/**
	 * The unexpected token
	 */
	public final Token token;
	public final Tokenizable<?>[] expected;

	public UnexpectedTokenException(Token token) {
		this(token, (Token) null);
	}

	public UnexpectedTokenException(Token token, Tokenizable<?>... expected) {
		super(message(token, expected), token.source, token.offset);
		this.token = token;
		this.expected = expected;
	}

	private static String message(Token token, Tokenizable<?>... expected) {
		String msg = "Unexpected token " + token.toString() + " at offset " + token.offset;
		if (expected.length != 0) {
			List<String> expct = Arrays.stream(expected).map(tk -> tk.token().toString()).collect(Collectors.toList());
			msg += " (expected " + String.join(", ", expct) + ")";
		}
		return msg;
	}
}
