package sqlartan.core.ast.gen;

import sqlartan.core.ast.Keyword;
import sqlartan.core.ast.Operator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import static sqlartan.core.ast.Keyword.NOT;
import static sqlartan.core.ast.Operator.*;
import static sqlartan.util.Matching.dispatch;

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
		last = (operator == LEFT_PAREN || operator == DOT) ? Spacing.NoSpace : Spacing.Space;
		return this;
	}

	public Builder append(Operator... operators) {
		for (Operator operator : operators) append(operator);
		return this;
	}

	public Builder append(KeywordOrOperator... items) {
		for (KeywordOrOperator item : items) {
			//noinspection Convert2MethodRef
			dispatch(item).when(Keyword.class, kw -> append(kw))
			              .when(Operator.class, op -> append(op));
		}
		return this;
	}

	/**
	 * Appends a Node to the output.
	 * The toSQL method on the given node will be called with this builder as parameter.
	 *
	 * @param node the node to append
	 */
	public Builder append(Buildable node) {
		node.toSQL(this);
		return this;
	}

	/**
	 * Appends a list of Nodes to the output, separated by the given separator.
	 *
	 * @param nodes     the list of nodes to append
	 * @param separator the separator between each element
	 */
	public <T> Builder append(List<? extends T> nodes, Function<? super T, ? extends Buildable> adapter, Buildable separator) {
		boolean first = true;
		for (T item : nodes) {
			if (first) {
				first = false;
			} else if (separator != Keyword.VOID) {
				separator.toSQL(this);
			}
			adapter.apply(item).toSQL(this);
		}
		return this;
	}

	public Builder append(List<? extends Buildable> nodes, Buildable separator) {
		return append(nodes, Function.identity(), separator);
	}

	/**
	 * Appends a list of Nodes to the output.
	 * The separator used will be the string ", ".
	 *
	 * @param nodes the list of nodes to append
	 */
	public <T> Builder append(List<? extends T> nodes, Function<? super T, ? extends Buildable> adapter) {
		return append(nodes, adapter, COMMA);
	}

	public Builder append(List<? extends Buildable> nodes) {
		return append(nodes, Function.identity(), COMMA);
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@SafeVarargs
	public final Builder append(Optional<? extends Buildable>... optionals) {
		for (Optional<? extends Buildable> optional : optionals) optional.ifPresent(this::append);
		return this;
	}

	public Builder appendUnary(KeywordOrOperator operator) {
		append(operator);
		if (operator != NOT) last = Spacing.NoSpace;
		return this;
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

	public Builder appendIdentifiers(List<String> identifiers, Buildable separator) {
		return append(identifiers, id -> sql -> sql.appendIdentifier(id), separator);
	}

	public Builder appendIdentifiers(List<String> identifiers) {
		return appendIdentifiers(identifiers, COMMA);
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

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public Builder appendSchema(Optional<String> schema) {
		schema.ifPresent(this::appendSchema);
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
