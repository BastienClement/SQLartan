package sqlartan.core.ast.token;

public class Identifier implements Token {
	public final String name;

	private Identifier(String name) {
		this.name = name;
	}

	public String toString() {
		return "Identifier(" + name + ")";
	}

	public static Identifier from(String name) {
		return new Identifier(name);
	}
}
