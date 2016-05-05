package sqlartan.core.ast.gen;

import sqlartan.core.ast.Keyword;
import sqlartan.core.ast.Node;
import sqlartan.core.ast.Operator;
import java.util.List;
import static sqlartan.core.ast.Operator.*;

/**
 * SQL Generator helper
 * This class wraps a StringBuilder object and provides utility functions for
 * appending Node and lists.
 */
public class Builder {
	/**
	 * Define default spacing for the next token
	 */
	private enum Spacing {
		Space, NoSpace
	}

	/**
	 * The internal StringBuilder object
	 */
	private StringBuilder builder = new StringBuilder();

	/**
	 * The last type of element appended to this builder
	 */
	private Spacing last = Spacing.NoSpace;

	public Builder append(Keyword keyword) {
		if (last == Spacing.Space) builder.append(" ");
		builder.append(keyword.name);
		last = Spacing.Space;
		return this;
	}

	public Builder append(Keyword... keywords) {
		for (Keyword keyword : keywords) append(keyword);
		return this;
	}

	public Builder append(Operator operator) {
		if (last == Spacing.Space && operator != COMMA && operator != RIGHT_PAREN && operator != DOT && operator != SEMICOLON) {
			builder.append(" ");
		}
		builder.append(operator.symbol);
		if (operator == SEMICOLON) builder.append("\n");
		last = (operator == LEFT_PAREN || operator == DOT || operator == SEMICOLON) ? Spacing.NoSpace : Spacing.Space;
		return this;
	}

	public Builder append(Operator... operators) {
		for (Operator operator : operators) append(operator);
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
	public Builder append(List<? extends Node> nodes, Node.Enumerated separator) {
		boolean first = true;
		for (Node node : nodes) {
			if (first) {
				first = false;
			} else {
				separator.toSQL(this);
			}
			node.toSQL(this);
		}
		return this;
	}

	/**
	 * Appends a list of Nodes to the output.
	 * The separator used will be the string ", ".
	 *
	 * @param nodes the list of nodes to append
	 */
	public Builder append(List<? extends Node> nodes) {
		return append(nodes, COMMA);
	}

	/**
	 * Appends an identifier to the output.
	 * The identifier will be escaped with brackets.
	 *
	 * @param identifier the identifier to append
	 */
	public Builder appendIdentifier(String identifier) {
		if (last == Spacing.Space) builder.append(" ");
		builder.append("[").append(identifier).append("]");
		last = Spacing.Space;
		return this;
	}

	public Builder appendRaw(String fragment) {
		if (last == Spacing.Space) builder.append(" ");
		builder.append(fragment);
		last = Spacing.Space;
		return this;
	}

	/**
	 * Appends a text literal to the output.
	 * Any apostrophe in the given string will be escaped.
	 *
	 * @param string the string to append
	 */
	public Builder appendTextLiteral(String string) {
		if (last == Spacing.Space) builder.append(" ");
		builder.append("'").append(string.replace("'", "''")).append("'");
		last = Spacing.Space;
		return this;
	}

	/**
	 * Appends a schema name to the output.
	 *
	 * @param schema the schema name
	 */
	public Builder appendSchema(String schema) {
		appendIdentifier(schema).append(DOT);
		return this;
	}

	/**
	 * Returns the resulting SQL code from this SQLBuilder.
	 */
	@Override
	public String toString() {
		return builder.toString().trim();
	}
}
