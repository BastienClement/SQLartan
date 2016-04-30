package sqlartan.core.ast.token;

public class Literal extends Token {
	public final String value;

	private Literal(String value, int offset) {
		super(offset);
		this.value = value;
	}

	protected String type() { return "Literal"; }
	protected String value() { return value; }

	public static Literal from(String value, int offset) {
		return new Literal(value, offset);
	}
}
