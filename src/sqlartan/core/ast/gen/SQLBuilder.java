package sqlartan.core.ast.gen;

import sqlartan.core.ast.Node;
import java.util.List;

public class SQLBuilder {
	private StringBuilder builder = new StringBuilder();

	public SQLBuilder append(String part) {
		builder.append(part);
		return this;
	}

	public SQLBuilder append(Node node) {
		node.toSQL(this);
		return this;
	}

	public SQLBuilder append(List<? extends Node> nodes, String separator) {
		boolean first = true;
		for (Node node : nodes) {
			if (first) {
				first = false;
			} else {
				builder.append(separator);
			}
			node.toSQL(this);
		}
		return this;
	}

	public SQLBuilder append(List<? extends Node> nodes) {
		return append(nodes, ", ");
	}

	public SQLBuilder appendIdentifier(String identifier) {
		builder.append("[").append(identifier).append("]");
		return this;
	}

	public SQLBuilder appendTextLiteral(String string) {
		builder.append("'").append(string.replace("'", "''")).append("'");
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}
