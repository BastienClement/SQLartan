package sqlartan.core.ast.token;

public class IdentifierToken extends Token<String> {
	public final boolean strict;

	private IdentifierToken(String name, String source, int offset, boolean strict) {
		super(TokenType.IDENTIFIER, source, offset, name);
		this.strict = strict;
	}


	public static IdentifierToken from(String value, String source, int offset, boolean strict) {
		return new IdentifierToken(value, source, offset, strict);
	}

	public LiteralToken.Text toLiteral() {
		return LiteralToken.fromText(value, source, offset);
	}
}
