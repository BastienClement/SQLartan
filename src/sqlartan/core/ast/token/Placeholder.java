package sqlartan.core.ast.token;

public abstract class Placeholder implements Token {
	public static class Indexed extends Placeholder {
		public final int index;

		public Indexed(int index) {
			this.index = index;
		}

		public String toString() {
			return "Placeholder(?" + index + ")";
		}
	}

	public static class Named extends Placeholder {
		public final String name;

		public Named(String name) {
			this.name = name;
		}

		public String toString() {
			return "Placeholder(:" + name + ")";
		}
	}
}
