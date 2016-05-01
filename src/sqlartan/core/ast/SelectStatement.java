package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.ArrayList;
import java.util.List;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.core.ast.token.Keyword.NOT;
import static sqlartan.core.ast.token.Operator.*;

public interface SelectStatement extends Statement {
	static SelectStatement parse(ParserContext context) {
		return context.parse(Simple::parse);
	}

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

	class Simple extends Core {
		static Simple parse(ParserContext context) {
			if (context.tryConsume(WITH)) {
				// TODO: parse common-table-expression list
				throw new UnsupportedOperationException();
			}

			Simple select = new Simple();
			Core.parse(context, select);

			return select;
		}
	}



	class SelectSource implements Node {
		public static SelectSource parse(ParserContext context) {
			return context.alternatives(
				() -> context.parse(TableOrSubquerySource::parse),
				() -> context.parse(JoinClause::parse)
			);
		}
	}

	class TableOrSubquerySource extends SelectSource {
		public static TableOrSubquerySource parse(ParserContext context) {
			if (context.current().equals(LEFT_PAREN)) {
				throw new UnsupportedOperationException();
			} else {
				return context.parse(TableSource::parse);
			}
		}

		public String as;
	}

	class TableSource extends TableOrSubquerySource {
		public static TableSource parse(ParserContext context) {
			TableSource table = new TableSource();
			if (context.next().equals(DOT)) {
				table.schema = context.consumeIdentifier().value;
				context.consume(DOT);
			}

			table.table = context.consumeIdentifier().value;

			context.tryConsume(AS);
			table.as = context.optConsumeIdentifier().map(Token::value).orElse(null);

			if (context.tryConsume(INDEXED)) {
				context.consume(BY);
				table.index = context.consumeIdentifier().value;
			} else if (context.tryConsume(NOT)) {
				context.consume(INDEXED);
				table.notIndexed = true;
			}

			return table;
		}

		public String schema;
		public String table;
		public String index;
		public boolean notIndexed;

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

	class JoinClause extends SelectSource {
		public static JoinClause parse(ParserContext context) {
			throw new UnsupportedOperationException();
		}
	}
}
