package sqlartan.core.ast.token;

public abstract class Placeholder<T> extends Token<T> {
	private Placeholder(String source, int offset, T value) {
		super(Type.PLACEHOLDER, source, offset, value);
	}

	public static class Indexed extends Placeholder<Integer> {
		private Indexed(int index, String source, int offset) {
			super(source, offset, index);
		}

		@Override
		public String value() { return "?" + value; }
	}

	public static class Named extends Placeholder<String> {
		private Named(String name, String source, int offset) {
			super(source, offset, name);
		}

		@Override
		public String value() { return "?" + value; }
	}

	public static Placeholder<Integer> forIndex(int index, String source, int offset) {
		return new Indexed(index, source, offset);
	}

	public static Placeholder forName(String name, String source, int offset) {
		return new Named(name, source, offset);
	}
}
