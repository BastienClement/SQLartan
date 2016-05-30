package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.COMMA;
import static sqlartan.core.ast.Operator.LEFT_PAREN;
import static sqlartan.core.ast.Operator.RIGHT_PAREN;

/**
 * https://www.sqlite.org/syntax/table-or-subquery.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class SelectSource implements Node {
	public Optional<String> alias = Optional.empty();

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static SelectSource parse(ParserContext context) {
		SelectSource lhs = parseSimple(context);

		boolean comma;
		while ((comma = context.current(COMMA)) || matchAny(context, NATURAL, LEFT, INNER, CROSS, JOIN)) {
			JoinClause join = new JoinClause();
			join.lhs = lhs;
			join.natural = context.tryConsume(NATURAL);
			join.join = JoinClause.Join.parse(context);
			join.rhs = parseSimple(context);
			if (!comma) {
				join.constraint = context.optParse(JoinConstraint::parse);
			}
			lhs = join;
		}

		return lhs;
	}

	private static boolean matchAny(ParserContext context, KeywordOrOperator... cases) {
		for (KeywordOrOperator koo : cases) {
			if (context.current(koo)) {
				return true;
			}
		}
		return false;
	}

	private static SelectSource parseSimple(ParserContext context) {
		if (context.current(LEFT_PAREN)) {
			if (context.next(SELECT) || context.next(VALUES)) {
				return Subquery.parse(context);
			} else {
				return Group.parse(context);
			}
		} else {
			context.begin();
			context.optConsumeSchema();
			if (context.next(LEFT_PAREN)) {
				context.rollback();
				return Function.parse(context);
			} else {
				context.rollback();
				return QualifiedTableName.parse(context, true);
			}
		}
	}

	protected static Optional<String> parseAlias(ParserContext context) {
		return context.tryConsume(AS)
			? Optional.of(context.consumeIdentifier())
			: context.optConsumeIdentifier();
	}

	/**
	 * Function call
	 */
	public static class Function extends SelectSource {
		public Optional<String> schema = Optional.empty();
		public String name;
		public List<Expression> args = new ArrayList<>();

		public static Function parse(ParserContext context) {
			Function function = new Function();
			function.schema = context.optConsumeSchema();
			function.name = context.consumeIdentifier();
			context.consume(LEFT_PAREN);
			context.parseList(function.args, Expression::parse);
			context.consume(RIGHT_PAREN);
			function.alias = parseAlias(context);
			return function;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.appendSchema(schema).appendIdentifier(name)
			   .append(LEFT_PAREN).append(args).append(RIGHT_PAREN);
			alias.ifPresent(a -> sql.append(AS).appendIdentifier(a));
		}
	}

	/**
	 * Sub-select source group
	 */
	public static class Group extends SelectSource {
		public SelectSource source;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Group parse(ParserContext context) {
			Group group = new Group();
			context.consume(LEFT_PAREN);
			group.source = SelectSource.parse(context);
			context.consume(RIGHT_PAREN);
			return group;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(LEFT_PAREN).append(source).append(RIGHT_PAREN);
		}
	}

	/**
	 * Subquery
	 */
	public static class Subquery extends SelectSource {
		public SelectStatement query;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Subquery parse(ParserContext context) {
			Subquery subquery = new Subquery();
			context.consume(LEFT_PAREN);
			subquery.query = SelectStatement.parse(context);
			context.consume(RIGHT_PAREN);
			subquery.alias = parseAlias(context);
			return subquery;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(LEFT_PAREN).append(query).append(RIGHT_PAREN);
			alias.ifPresent(a -> sql.append(AS).appendIdentifier(a));
		}
	}
}
