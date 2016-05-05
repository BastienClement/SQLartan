package sqlartan.core.ast.gen;

import sqlartan.core.ast.Node;
import java.util.List;

/**
 * SQL Generator helper
 * This class wraps a StringBuilder object and provides utility functions for
 * appending Node and lists.
 */
public class Builder {
	/**
	 * The internal StringBuilder object
	 */
	private StringBuilder builder = new StringBuilder();

	/**
	 * Appends a string to the output.
	 *
	 * @param part the string to append
	 */
	public Builder append(String part) {
		builder.append(part);
		return this;
	}

	/**
	 * Appends a Node to the output.
	 * The toSQL method on the given node will be called with this builder as parameter.
	 *
	 * @param node the node to append
	 */
	public Builder append(Node node) {
		node.toSQL(this);
		return this;
	}

	/**
	 * Appends a list of Nodes to the output, separated by the given separator.
	 *
	 * @param nodes     the list of nodes to append
	 * @param separator the separator between each element
	 */
	public Builder append(List<? extends Node> nodes, String separator) {
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

	/**
	 * Appends a list of Nodes to the output.
	 * The separator used will be the string ", ".
	 * @param nodes the list of nodes to append
	 */
	public Builder append(List<? extends Node> nodes) {
		return append(nodes, ", ");
	}

	/**
	 * Appends an identifier to the output.
	 * The identifier will be escaped with brackets.
	 * @param identifier the identifier to append
	 */
	public Builder appendIdentifier(String identifier) {
		builder.append("[").append(identifier).append("]");
		return this;
	}

	/**
	 * Appends a text literal to the output.
	 * Any apostrophe in the given string will be escaped.
	 *
	 * @param string the string to append
	 */
	public Builder appendTextLiteral(String string) {
		builder.append("'").append(string.replace("'", "''")).append("'");
		return this;
	}

	/**
	 * Appends a schema name to the output.
	 *
	 * @param schema the schema name
	 */
	public Builder appendSchema(String schema) {
		builder.append(schema).append(".");
		return this;
	}

	/**
	 * Returns the resulting SQL code from this SQLBuilder.
	 */
	@Override
	public String toString() {
		return builder.toString();
	}
}
