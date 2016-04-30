package sqlartan.core.ast.token;

public abstract class Token {
	public final int offset;

	protected Token(int offset) {
		this.offset = offset;
	}

	protected abstract String type();
	protected abstract String value();

	@Override
	public String toString() {
		return type() + "(" + value() + ", " + offset + ")";
	}
}
