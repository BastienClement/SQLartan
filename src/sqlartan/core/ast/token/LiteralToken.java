package sqlartan.core.ast.token;

public class LiteralToken extends Token<String> {
	private LiteralToken(String value, String source, int offset) {
		super(TokenType.LITERAL, source, offset, value);
	}

	public static class Text extends LiteralToken {
		private Text(String value, String source, int offset) {
			super(value, source, offset);
		}

		public IdentifierToken toIdentifier() {
			return IdentifierToken.from(value, source, offset, false);
		}
	}

	public static class Numeric extends LiteralToken {
		private Numeric(String value, String source, int offset) {
			super(value, source, offset);
		}
	}

	public static Text fromText(String value, String source, int offset) {
		return new Text(value, source, offset);
	}

	public static Numeric fromNumeric(String value, String source, int offset) {
		return new Numeric(value, source, offset);
	}
}
