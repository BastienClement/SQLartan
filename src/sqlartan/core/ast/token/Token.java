package sqlartan.core.ast.token;

public abstract class Token<T> {
	public final Type type;
	public final String source;
	public final int offset;
	public final T value;

	protected Token(Type type, String source, int offset, T value) {
		this.type = type;
		this.source = source;
		this.offset = offset;
		this.value = value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!(other instanceof Token)) {
			return false;
		} else {
			Token token = (Token) other;
			return type == token.type && ((value != null && value.equals(token.value)) || token.value == null);
		}
	}

	@Override
	public int hashCode() {
		if (value != null) {
			int hash = 17;
			hash = hash * 31 + type.hashCode();
			hash = hash * 31 + value.hashCode();
			return hash;
		} else {
			return type.hashCode();
		}
	}

	public String value() {
		return value != null ? value.toString() : "";
	}

	@Override
	public String toString() {
		return type.label + "(" + value() + ")";
	}
}
