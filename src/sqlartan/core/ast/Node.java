package sqlartan.core.ast;

public interface Node {
	default String toSQL() {
		throw new UnsupportedOperationException();
	}
}
