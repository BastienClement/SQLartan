package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.ArrayList;
import java.util.List;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.core.ast.token.Keyword.NOT;
import static sqlartan.core.ast.token.Operator.*;

public interface SelectStatement extends Statement {
	/**
	 * General SELECT statement parser
	 * Currently delegates to Simple parser.
	 * TODO: handle compound selects here
	 */
	static SelectStatement parse(ParserContext context) {
		return context.parse(Simple::parse);
	}

	/**
	 * The core a SELECT statement
	 */
	class Core implements SelectStatement {
		public boolean distinct;
		public List<ResultColumn> columns;
		public List<SelectSource> from;
		public Expression where;
		public List<Expression> groupBy;
		public Expression having;

		static Core parse(ParserContext context) {
			Core select = new Core();
			parse(context, select);
			return select;
		}

		static void parse(ParserContext context, Core select) {
			context.consume(SELECT);

			select.distinct = context.tryConsume(DISTINCT) && !context.tryConsume(ALL);
			select.columns = context.parseList(COMMA, ResultColumn::parse);

			if (context.tryConsume(FROM)) {
				select.from = new ArrayList<>();
				if (!context.parseList(select.from, COMMA, TableOrSubquerySource::parse)) {
					select.from.add(context.parse(JoinClause::parse));
				}
			}

			if (context.tryConsume(WHERE)) {
				select.where = context.parse(Expression::parse);
			}

			if (context.tryConsume(GROUP)) {
				context.consume(BY);
				select.groupBy = context.parseList(COMMA, Expression::parse);

				if (context.tryConsume(HAVING)) {
					select.having = context.parse(Expression::parse);
				}
			}
		}

		@Override
		public void toSQL(StringBuilder sb) {
			sb.append("SELECT ");
			if (distinct)
				sb.append("DISTINCT ");
			joinNodes(sb, ", ", columns);
			if (from != null)
				joinNodes(sb.append(" FROM "), ", ", from);
			if (where != null)
				where.toSQL(sb.append(" WHERE "));
			if (groupBy != null) {
				joinNodes(sb.append(" GROUP BY "), ",", groupBy);
				if (having != null)
					having.toSQL(sb.append(" HAVING "));
			}
		}
	}

	/**
	 * A simple SELECT statement
	 */
	class Simple extends Core {
		public List<OrderingTerm> orderBy;
		public Expression limit;
		public Expression offset;

		static Simple parse(ParserContext context) {
			if (context.tryConsume(WITH)) {
				// With clauses are unsupported
				throw new UnsupportedOperationException();
			}

			// Parse the core
			Simple select = new Simple();
			Core.parse(context, select);

			// ORDER BY
			if (context.tryConsume(ORDER)) {
				context.consume(BY);
				select.orderBy = context.parseList(COMMA, OrderingTerm::parse);
			}

			// LIMIT .. OFFSET
			if (context.tryConsume(LIMIT)) {
				select.limit = context.parse(Expression::parse);
				if (context.tryConsume(OFFSET) || context.tryConsume(COMMA)) {
					select.offset = context.parse(Expression::parse);
				}
			}

			return select;
		}

		@Override
		public void toSQL(StringBuilder sb) {
			super.toSQL(sb);
			if (orderBy != null)
				joinNodes(sb.append(" ORDER BY "), ", ", orderBy);
			if (limit != null) {
				limit.toSQL(sb.append(" LIMIT "));
				if (offset != null) {
					offset.toSQL(sb.append(", "));
				}
			}
		}
	}

	/**
	 * Source of data for SELECT statements
	 */
	class SelectSource implements Node {
		public static SelectSource parse(ParserContext context) {
			return context.alternatives(
				() -> context.parse(TableOrSubquerySource::parse),
				() -> context.parse(JoinClause::parse)
			);
		}
	}

	/**
	 * Either a table or a sub-query sources
	 */
	class TableOrSubquerySource extends SelectSource {
		public String as;

		public static TableOrSubquerySource parse(ParserContext context) {
			if (context.current(LEFT_PAREN)) {
				// Sub query
				throw new UnsupportedOperationException();
			} else {
				return context.parse(TableSource::parse);
			}
		}
	}

	/**
	 * A table source
	 */
	class TableSource extends TableOrSubquerySource {
		public String schema;
		public String table;
		public String index;
		public boolean notIndexed;

		public static TableSource parse(ParserContext context) {
			TableSource source = new TableSource();

			// Explicit schema name
			if (context.next(DOT)) {
				source.schema = context.consumeIdentifier().value;
				context.consume(DOT);
			}

			// Table name
			source.table = context.consumeIdentifier().value;

			// Attempt to consume alias
			context.tryConsume(AS);
			source.as = context.optConsumeIdentifier().map(Token::value).orElse(null);

			if (context.tryConsume(INDEXED)) {
				context.consume(BY);
				source.index = context.consumeIdentifier().value;
			} else if (context.tryConsume(NOT)) {
				context.consume(INDEXED);
				source.notIndexed = true;
			}

			return source;
		}

		@Override
		public void toSQL(StringBuilder sb) {
			if (schema != null)
				sb.append(schema).append(".");
			sb.append(table);
			if (as != null)
				sb.append(" AS ").append(as);
			if (notIndexed)
				sb.append(" NOT INDEXED");
			else if (index != null)
				sb.append(" INDEXED BY ").append(index);
		}
	}

	/**
	 * A JOIN clause
	 */
	class JoinClause extends SelectSource {
		public static JoinClause parse(ParserContext context) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Term for ORDER BY clause
	 */
	class OrderingTerm implements Node {
		static OrderingTerm parse(ParserContext context) {
			throw new UnsupportedOperationException();
		}
	}
}
