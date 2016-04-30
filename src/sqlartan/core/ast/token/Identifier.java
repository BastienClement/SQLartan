package sqlartan.core.ast.token;

public class Identifier extends Token {
	public final String name;

	private Identifier(String name, String source, int offset) {
		super(source, offset);
		this.name = name;
	}

	protected String type() { return "Identifier"; }
	protected String value() { return name; }

	public static Identifier from(String value, String source, int offset) {
		return new Identifier(value, source, offset);
	}
}
