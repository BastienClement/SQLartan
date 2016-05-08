package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.COMMA;

/**
 * https://www.sqlite.org/syntaxdiagrams.html#select-core
 */
@SuppressWarnings({ "WeakerAccess", "OptionalUsedAsFieldOrParameterType" })
public class SelectCoreStatement extends SelectStatement.CoreProperties implements SelectStatement.Core {
	public static SelectCoreStatement parse(ParserContext context) {
		context.consume(SELECT);
		SelectCoreStatement select = new SelectCoreStatement();

		select.distinct = context.tryConsume(DISTINCT);
		if (!select.distinct) context.tryConsume(ALL);

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
}
