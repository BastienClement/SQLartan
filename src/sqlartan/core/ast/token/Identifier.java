package sqlartan.core.ast.token;

/**
 * An identifier token
 */
public class Identifier extends Token<String> {
	/**
	 * Strict flag
	 * A identifier is strict if it was escaped with [] or ``.
	 * A strict identifier will not be implicitly converted to a text literal during parsing.
	 */
	public final boolean strict;

	private Identifier(String name, String source, int offset, boolean strict) {
		super(TokenType.IDENTIFIER, source, offset, name);
		this.strict = strict;
	}

	/**
	 * Constructs a Identifier token.
	 *
	 * @param value  the identifier name
	 * @param source the token source code
	 * @param offset the token offset in the source code
	 * @param strict whether this token is strict or not
	 */
	public static Identifier from(String value, String source, int offset, boolean strict) {
		return new Identifier(value, source, offset, strict);
	}

	/**
	 * Returns a Text Literal token with the same value as this Identifier.
	 */
	public Literal.Text toLiteral() {
		return Literal.fromText(value, source, offset);
	}
}
