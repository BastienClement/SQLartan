package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Keyword.HAVING;
import static sqlartan.core.ast.Operator.COMMA;

/**
 * https://www.sqlite.org/syntaxdiagrams.html#select-core
 */
@SuppressWarnings({ "WeakerAccess", "OptionalUsedAsFieldOrParameterType" })
public class SelectCoreStatement extends SelectStatement.Core {
	public boolean distinct;
	public List<ResultColumn> columns = new ArrayList<>();
	public Optional<SelectSource> from = Optional.empty();
	public Optional<WhereClause> where = Optional.empty();
	public List<Expression> groupBy = new ArrayList<>();
	public Optional<Expression> having = Optional.empty();

	public static SelectCoreStatement parse(ParserContext context) {
		SelectCoreStatement select = new SelectCoreStatement();
		context.consume(SELECT);

		select.distinct = context.tryConsume(DISTINCT) || !context.tryConsume(ALL);
		select.columns = context.parseList(ResultColumn::parse);

		if (context.tryConsume(FROM)) {
			select.from = Optional.of(SelectSource.parse(context));
		}

		if (context.current(WHERE)) {
			select.where = Optional.of(WhereClause.parse(context));
		}

		if (context.tryConsume(GROUP, BY)) {
			select.groupBy = context.parseList(COMMA, Expression::parse);

			if (context.tryConsume(HAVING)) {
				select.having = Optional.of(Expression.parse(context));
			}
		}

		return select;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(SELECT);
		if (distinct) sql.append(DISTINCT);
		sql.append(columns);
		from.ifPresent(f -> sql.append(FROM).append(f));
		sql.append(where);
		if (!groupBy.isEmpty()) {
			sql.append(GROUP, BY).append(groupBy);
			having.ifPresent(h -> sql.append(HAVING).append(h));
		}
	}
}
