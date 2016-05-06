package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.LIMIT;
import static sqlartan.core.ast.Keyword.OFFSET;
import static sqlartan.core.ast.Operator.COMMA;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class LimitClause implements Node {
	public Expression expression;
	public Optional<Expression> offset = Optional.empty();

	public static LimitClause parse(ParserContext context) {
		LimitClause limit = new LimitClause();
		context.consume(LIMIT);
		limit.expression = Expression.parse(context);
		if (context.tryConsume(OFFSET) || context.tryConsume(COMMA)) {
			limit.offset = Optional.of(Expression.parse(context));
		}
		return limit;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(LIMIT).append(expression);
		offset.ifPresent(o -> sql.append(COMMA).append(o));
	}
}
