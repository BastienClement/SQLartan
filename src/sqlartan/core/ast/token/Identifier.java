package sqlartan.core.ast.token;

public class Identifier extends Token<String> {
	private Identifier(String name, String source, int offset) {
		super(Type.IDENTIFIER, source, offset, name);
	}

	public static Identifier from(String value, String source, int offset) {
		return new Identifier(value, source, offset);
	}
}
