package sqlartan.core.ast.token;

public interface Tokenizable<T extends Token> {
	T token();

	interface Self<S extends Token> extends Tokenizable<S> {
		@SuppressWarnings("unchecked")
		default S token() { return (S) this; }
	}
}

