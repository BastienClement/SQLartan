package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;

/**
 * An unexpected token occurred while parsing the input
 */
public class UnexpectedTokenException extends ParseException {
	/**
	 * The unexpected token
	 */
	public final Token token;

	public UnexpectedTokenException(Token token) {
		super("Unexpected token " + token.toString() + " at offset " + token.offset, token.source, token.offset);
		this.token = token;
	}
}
