package sqlartan.core.ast.token;

public class Identifier extends Token {
	public final String name;

	private Identifier(String name, int offset) {
		super(offset);
		this.name = name;
	}

	protected String type() { return "Identifier"; }
	protected String value() { return name; }

	public static Identifier from(String value, int offset) {
		return new Identifier(value, offset);
	}
}
