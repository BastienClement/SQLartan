package sqlartan.core.ast.gen;

import sqlartan.core.ast.Keyword;
import sqlartan.core.ast.Operator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import static sqlartan.core.ast.Keyword.NOT;
import static sqlartan.core.ast.Operator.*;
import static sqlartan.util.Matching.dispatch;

/**
 * The SQL Generator helper.
 * <p>
 * This class wraps a StringBuilder object and provides utility functions for
 * appending Node and lists. Most methods of this class returns the object
 * itself, allowing the calls to be chained.
 */
@SuppressWarnings({ "WeakerAccess", "OptionalUsedAsFieldOrParameterType" })
public class Builder {
	/**
	 * Defines default spacing for the next token.
	 * Some tokens might ignore this indication in certain context.
	 */
	private enum Spacing {
		Space, NoSpace
	}

	/**
	 * The internal StringBuilder object.
	 */
	private StringBuilder builder = new StringBuilder();

	/**
	 * The spacing property of the last element appended to this builder.
	 */
	private Spacing last = Spacing.NoSpace;

	/**
	 * Appends a single keyword.
	 *
	 * @param keyword the SQL keyword to append
	 */
	public Builder append(Keyword keyword) {
		if (last == Spacing.Space) builder.append(" ");
		builder.append(keyword.name);
		last = Spacing.Space;
		return this;
	}

	/**
	 * Appends multiple keywords.
	 *
	 * @param keywords multiple keywords to append to this Builder
	 */
	public Builder append(Keyword... keywords) {
		for (Keyword keyword : keywords) append(keyword);
		return this;
	}

	/**
	 * Appends a single operator.
	 *
	 * @param operator the operator to append
	 */
	public Builder append(Operator operator) {
		if (last == Spacing.Space && operator != COMMA && operator != RIGHT_PAREN && operator != DOT && operator != SEMICOLON) {
			builder.append(" ");
		}
		builder.append(operator.symbol);
		last = (operator == LEFT_PAREN || operator == DOT) ? Spacing.NoSpace : Spacing.Space;
		return this;
	}

	/**
	 * Appends multiple operators.
	 *
	 * @param operators multiple operators to append to this Builder
	 */
	public Builder append(Operator... operators) {
		for (Operator operator : operators) append(operator);
		return this;
	}

	/**
	 * Appends multiple keywords or operators.
	 * The runtime type of each arguments is used to dispatch the elements
	 * between append(Operator) and append(Keyword).
	 *
	 * @param items the items to append to this Builder
	 */
	public Builder append(KeywordOrOperator... items) {
		for (KeywordOrOperator item : items) {
			dispatch(item).when(Keyword.class, (Consumer<Keyword>) this::append)
			              .when(Operator.class, (Consumer<Operator>) this::append);
		}
		return this;
	}

	/**
	 * Appends an unary operator.
	 * Unary operators follow different spacing rule than equivalent binary operators.
	 *
	 * @param operator the unary operator to append
	 */
	public Builder appendUnary(KeywordOrOperator operator) {
		append(operator);
		if (operator != NOT) last = Spacing.NoSpace;
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
	 * Appends a list of elements to the output, separated by the given separator.
	 * <p>
	 * The given adapter will be called on each elements of the list to convert it
	 * to a Buildable instance.
	 *
	 * @param items     a list of items to append to the output
	 * @param adapter   an adapter that can convert an item of type T to a Buildable
	 * @param separator the separator to use between the items
	 * @param <T>       the initial type of the items
	 */
	public <T> Builder append(List<? extends T> items, Function<? super T, ? extends Buildable> adapter, Buildable separator) {
		boolean first = true;
		for (T item : items) {
			if (first) {
				first = false;
			} else if (separator != Keyword.VOID) {
				separator.toSQL(this);
			}
			adapter.apply(item).toSQL(this);
		}
		return this;
	}

	/**
	 * Appends a list of Buildable instances to the output, separated by the given separator.
	 *
	 * @param items     a list of Buildable to append
	 * @param separator the seoarator to use between the items
	 */
	public Builder append(List<? extends Buildable> items, Buildable separator) {
		return append(items, Function.identity(), separator);
	}

	/**
	 * Appends a list of items to the output, separated by a comma.
	 *
	 * @param items   a list of items to append to the output
	 * @param adapter an adapter that can convert an item of type T to a Buildable
	 * @param <T>     the initial type of the items
	 */
	public <T> Builder append(List<? extends T> items, Function<? super T, ? extends Buildable> adapter) {
		return append(items, adapter, COMMA);
	}

	/**
	 * Appends a list of Buildable instances to the output, separated by a comma.
	 *
	 * @param items a list of Buildable to append
	 */
	public Builder append(List<? extends Buildable> items) {
		return append(items, Function.identity(), COMMA);
	}

	/**
	 * Appends multiple optional Buildable to the output.
	 * Each item will only be appended if the optional is non-empty.
	 *
	 * @param optionals multiple optionals to append to the output
	 */
	@SafeVarargs
	public final Builder append(Optional<? extends Buildable>... optionals) {
		for (Optional<? extends Buildable> optional : optionals) optional.ifPresent(this::append);
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

	/**
	 * Appends a list of identifiers, separated by the given separator.
	 *
	 * @param identifiers the identifiers to append
	 * @param separator   the separator to use between identifiers
	 */
	public Builder appendIdentifiers(List<String> identifiers, Buildable separator) {
		return append(identifiers, id -> sql -> sql.appendIdentifier(id), separator);
	}

	/**
	 * Appends a list of identifiers, separated by a comma.
	 *
	 * @param identifiers the list of identifiers to append
	 */
	public Builder appendIdentifiers(List<String> identifiers) {
		return appendIdentifiers(identifiers, COMMA);
	}

	/**
	 * Appends a raw SQL fragment.
	 *
	 * @param fragment the SQL fragment to append
	 */
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
	 * Optionally appends a schema, does nothing if schema is empty.
	 *
	 * @param schema the schema name to append
	 */
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
