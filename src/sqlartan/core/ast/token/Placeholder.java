package sqlartan.core.ast.token;

public abstract class Placeholder extends Token {
	private Placeholder(int offset) {
		super(offset);
	}

	protected String type() { return "Placeholder"; }

	public static class Indexed extends Placeholder {
		public final int index;

		Indexed(int index, int offset) {
			super(offset);
			this.index = index;
		}

		protected String value() { return "?" + index; }
	}

	public static class Named extends Placeholder {
		public final String name;

		Named(String name, int offset) {
			super(offset);
			this.name = name;
		}

		protected String value() { return ":" + name; }
	}
}
