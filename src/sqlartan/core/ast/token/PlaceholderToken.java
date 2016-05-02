package sqlartan.core.ast.token;

public abstract class PlaceholderToken<T> extends Token<T> {
	private PlaceholderToken(String source, int offset, T value) {
		super(TokenType.PLACEHOLDER, source, offset, value);
	}

	public static class Indexed extends PlaceholderToken<Integer> {
		private Indexed(int index, String source, int offset) {
			super(source, offset, index);
		}

		@Override
		public String stringValue() { return "?" + value; }
	}

	public static class Named extends PlaceholderToken<String> {
		private Named(String name, String source, int offset) {
			super(source, offset, name);
		}

		@Override
		public String stringValue() { return "?" + value; }
	}

	public static Indexed forIndex(int index, String source, int offset) {
		return new Indexed(index, source, offset);
	}

	public static Named forName(String name, String source, int offset) {
		return new Named(name, source, offset);
	}
}
