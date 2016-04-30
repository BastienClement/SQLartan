package sqlartan.core.ast.token;

public class Literal implements Token {
	public final String value;

	private Literal(String value) {
		this.value = value;
	}

	public String toString() {
		return "Literal(" + value + ")";
	}

	public static Literal from(String value) {
		return new Literal(value);
	}
}
