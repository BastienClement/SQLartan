package sqlartan.core.ast;

public interface Node {
	default String toSQL() {
		StringBuilder sb = new StringBuilder();
		toSQL(sb);
		return sb.toString();
	}

	default void toSQL(StringBuilder sb) {
		throw new UnsupportedOperationException();
	}

	default void joinNodes(StringBuilder sb, String delim, Iterable<? extends Node> nodes) {
		boolean first = true;
		for (Node node : nodes) {
			if (first) {
				first = false;
			} else {
				sb.append(delim);
			}
			sb.append(node.toSQL());
		}
	}
}
