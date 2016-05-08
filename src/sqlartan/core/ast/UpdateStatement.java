package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.EQ;

/**
 * https://www.sqlite.org/lang_update.html
 */
@SuppressWarnings({ "WeakerAccess", "OptionalUsedAsFieldOrParameterType" })
public class UpdateStatement implements Statement {
	public enum Fallback implements Node.Enumerated {
		Undefined(),
		OrRollback(OR, ROLLBACK),
		OrAbort(OR, ABORT),
		OrReplace(OR, REPLACE),
		OrFail(OR, FAIL),
		OrIgnore(OR, IGNORE);

		private Keyword[] keywords;

		Fallback(Keyword... keywords) {
			this.keywords = keywords;
		}

		public static Fallback parse(ParserContext context) {
			if (context.tryConsume(OR)) {
				switch (context.consume(Token.Keyword.class).node()) {
					case ROLLBACK:
						return OrRollback;
					case ABORT:
						return OrAbort;
					case REPLACE:
						return OrReplace;
					case FAIL:
						return OrFail;
					case IGNORE:
						return OrIgnore;
					default:
						throw ParseException.UnexpectedCurrentToken(ROLLBACK, ABORT, REPLACE, FAIL, IGNORE);
				}
			} else {
				return Undefined;
			}
		}

		@Override
		public void toSQL(Builder sql) {
			sql.append(keywords);
		}
	}

	public Fallback fallback = Fallback.Undefined;
	public QualifiedTableName table;
	public List<SetExpression> set = new ArrayList<>();
	public Optional<WhereClause> where = Optional.empty();
	public Optional<OrderByClause> orderBy = Optional.empty();
	public Optional<LimitClause> limit = Optional.empty();

	public static UpdateStatement parse(ParserContext context) {
		UpdateStatement update = new UpdateStatement();
		context.consume(UPDATE);
		update.fallback = Fallback.parse(context);
		update.table = QualifiedTableName.parse(context);
		context.consume(SET);
		update.set = context.parseList(SetExpression::parse);
		if (context.current(WHERE)) {
			update.where = Optional.of(WhereClause.parse(context));
		}
		if (context.current(ORDER)) {
			update.orderBy = Optional.of(OrderByClause.parse(context));
		}
		if (update.orderBy.isPresent() || context.current(LIMIT)) {
			update.limit = Optional.of(LimitClause.parse(context));
		}
		return update;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(UPDATE).append(fallback).append(table)
		   .append(SET).append(set)
		   .append(where, orderBy, limit);
	}

	/**
	 * column = expr
	 */
	public static class SetExpression implements Node {
		public String column;
		public Expression value;

		public static SetExpression parse(ParserContext context) {
			SetExpression set = new SetExpression();
			set.column = context.consumeIdentifier();
			context.consume(EQ);
			set.value = Expression.parse(context);
			return set;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.appendIdentifier(column).append(EQ).append(value);
		}
	}
}
