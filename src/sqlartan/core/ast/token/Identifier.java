package sqlartan.core.ast.token;

public class Identifier extends Token<String> {
	public final boolean strict;

	private Identifier(String name, String source, int offset, boolean strict) {
		super(TokenType.IDENTIFIER, source, offset, name);
		this.strict = strict;
	}


	public static Identifier from(String value, String source, int offset, boolean strict) {
		return new Identifier(value, source, offset, strict);
	}

	public Literal.Text toLiteral() {
		return Literal.fromText(value, source, offset);
	}
}
