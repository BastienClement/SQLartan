package sqlartan.core.ast.token;

/**
 * Literal value
 */
public class Literal extends Token<String> {
	private Literal(String value, String source, int offset) {
		super(TokenType.LITERAL, source, offset, value);
	}

	/**
	 * A text literal
	 */
	public static class Text extends Literal {
		private Text(String value, String source, int offset) {
			super(value, source, offset);
		}

		public Identifier toIdentifier() {
			return Identifier.from(value, source, offset, false);
		}
	}

	/**
	 * Constructs a Text literal token.
	 *
	 * @param value  the literal value
	 * @param source the token source code
	 * @param offset the token offset in the source code
	 */
	public static Text fromText(String value, String source, int offset) {
		return new Text(value, source, offset);
	}

	/**
	 * A numeric literal
	 */
	public static class Numeric extends Literal {
		private Numeric(String value, String source, int offset) {
			super(value, source, offset);
		}
	}

	/**
	 * Constructs a Numeric literal token.
	 *
	 * @param value  the literal value
	 * @param source the token source code
	 * @param offset the token offset in the source code
	 */
	public static Numeric fromNumeric(String value, String source, int offset) {
		return new Numeric(value, source, offset);
	}
}
