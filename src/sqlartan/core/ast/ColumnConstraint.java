package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.LEFT_PAREN;
import static sqlartan.core.ast.Operator.RIGHT_PAREN;
import static sqlartan.util.Matching.match;

/**
 * https://www.sqlite.org/syntax/column-constraint.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class ColumnConstraint implements Node {
	public Optional<String> name = Optional.empty();

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static ColumnConstraint parse(ParserContext context) {
		Optional<String> name = context.tryConsume(CONSTRAINT)
			? Optional.of(context.consumeIdentifier())
			: Optional.empty();

		ColumnConstraint constraint = match(context.current(), ColumnConstraint.class)
			.when(PRIMARY, () -> PrimaryKey.parse(context))
			.when(NOT, () -> NotNull.parse(context))
			.when(UNIQUE, () -> Unique.parse(context))
			.when(CHECK, () -> Check.parse(context))
			.when(DEFAULT, () -> Default.parse(context))
			.when(COLLATE, () -> Collate.parse(context))
			.when(REFERENCES, () -> ForeignKey.parse(context))
			.orElseThrow(ParseException.UnexpectedCurrentToken(PRIMARY, NOT, UNIQUE, CHECK, DEFAULT, COLLATE, REFERENCES));

		constraint.name = name;
		return constraint;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		name.ifPresent(n -> sql.append(CONSTRAINT).appendIdentifier(n));
	}

	/**
	 * PRIMARY KEY ...
	 */
	public static class PrimaryKey extends ColumnConstraint {
		public Ordering ordering = Ordering.None;
		public ConflictClause onConflict = ConflictClause.None;
		public boolean autoincrement;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static PrimaryKey parse(ParserContext context) {
			context.consume(PRIMARY, KEY);
			PrimaryKey key = new PrimaryKey();
			key.ordering = Ordering.parse(context);
			key.onConflict = ConflictClause.parse(context);
			key.autoincrement = context.tryConsume(AUTOINCREMENT);
			return key;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(PRIMARY, KEY).append(ordering).append(onConflict);
			if (autoincrement) sql.append(AUTOINCREMENT);
		}
	}

	/**
	 * NOT NULL ...
	 */
	public static class NotNull extends ColumnConstraint {
		public ConflictClause onConflict = ConflictClause.None;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static NotNull parse(ParserContext context) {
			context.consume(NOT, NULL);
			NotNull notNull = new NotNull();
			notNull.onConflict = ConflictClause.parse(context);
			return notNull;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(NOT, NULL).append(onConflict);
		}
	}

	/**
	 * UNIQUE ...
	 */
	public static class Unique extends ColumnConstraint {
		public ConflictClause onConflict = ConflictClause.None;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Unique parse(ParserContext context) {
			context.consume(UNIQUE);
			Unique unique = new Unique();
			unique.onConflict = ConflictClause.parse(context);
			return unique;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(UNIQUE).append(onConflict);
		}
	}

	/**
	 * CHECK ...
	 */
	public static class Check extends ColumnConstraint {
		public Expression expression;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Check parse(ParserContext context) {
			context.consume(CHECK, LEFT_PAREN);
			Check check = new Check();
			check.expression = Expression.parse(context);
			context.consume(RIGHT_PAREN);
			return check;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(CHECK, LEFT_PAREN).append(expression).append(RIGHT_PAREN);
		}
	}

	/**
	 * DEFAULT ...
	 */
	public static abstract class Default extends ColumnConstraint {
		public static Default parse(ParserContext context) {
			return context.alternatives(Value::parse, Expr::parse);
		}

		public static class Value extends Default {
			public LiteralValue value;

			/**
			 * @see sqlartan.core.ast.parser.Parser
			 */
			public static Value parse(ParserContext context) {
				Value value = new Value();
				context.consume(DEFAULT);
				value.value = LiteralValue.parse(context);
				return value;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void toSQL(Builder sql) {
				sql.append(DEFAULT).append(value);
			}
		}

		public static class Expr extends Default {
			Expression expression;

			/**
			 * @see sqlartan.core.ast.parser.Parser
			 */
			public static Expr parse(ParserContext context) {
				context.consume(DEFAULT, LEFT_PAREN);
				Expr expr = new Expr();
				expr.expression = Expression.parse(context);
				context.consume(RIGHT_PAREN);
				return expr;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void toSQL(Builder sql) {
				sql.append(DEFAULT, LEFT_PAREN)
				   .append(expression)
				   .append(RIGHT_PAREN);
			}
		}
	}

	/**
	 * COLLATE ...
	 */
	public static class Collate extends ColumnConstraint {
		public String collation;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Collate parse(ParserContext context) {
			context.consume(COLLATE);
			Collate collate = new Collate();
			collate.collation = context.consumeIdentifier();
			return collate;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(COLLATE).appendIdentifier(collation);
		}
	}

	/**
	 * REFERENCES ...
	 */
	public static class ForeignKey extends ColumnConstraint {
		public ForeignKeyClause foreignKey;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static ForeignKey parse(ParserContext context) {
			ForeignKey fk = new ForeignKey();
			fk.foreignKey = ForeignKeyClause.parse(context);
			return fk;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(foreignKey);
		}
	}
}
