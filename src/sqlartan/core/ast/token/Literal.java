package sqlartan.core.ast.token;

public class Literal extends Token<String> {
	private Literal(String value, String source, int offset) {
		super(Type.LITERAL, source, offset, value);
	}

	public static Literal from(String value, String source, int offset) {
		return new Literal(value, source, offset);
	}
}
