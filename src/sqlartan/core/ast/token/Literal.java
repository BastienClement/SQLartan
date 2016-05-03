package sqlartan.core.ast.token;

public class Literal extends Token<String> {
	private Literal(String value, String source, int offset) {
		super(TokenType.LITERAL, source, offset, value);
	}

	public static class Text extends Literal {
		private Text(String value, String source, int offset) {
			super(value, source, offset);
		}

		public Identifier toIdentifier() {
			return Identifier.from(value, source, offset, false);
		}
	}

	public static class Numeric extends Literal {
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
