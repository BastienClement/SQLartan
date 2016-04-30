package sqlartan.core.ast.token;

public abstract class Token {
	public final String source;
	public final int offset;

	protected Token(String source, int offset) {
		this.source = source;
		this.offset = offset;
	}

	protected abstract String type();
	protected abstract String value();

	public String toString() {
		return type() + "(" + value() + ")";
	}
}
