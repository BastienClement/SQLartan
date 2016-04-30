package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;

public class UnexpectedTokenException extends ParseException {
	public final Token token;

	public UnexpectedTokenException(Token token) {
		super("Unexpected token", token.source, token.offset);
		this.token = token;
	}
}
