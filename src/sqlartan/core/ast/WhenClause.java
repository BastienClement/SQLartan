package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.THEN;
import static sqlartan.core.ast.Keyword.WHEN;

@SuppressWarnings("WeakerAccess")
public class WhenClause implements Node {
	public Expression when;
	public Expression then;

	public static WhenClause parse(ParserContext context) {
		context.consume(WHEN);
		WhenClause when = new WhenClause();
		when.when = Expression.parse(context);
		context.consume(THEN);
		when.then = Expression.parse(context);
		return when;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(WHEN).append(when)
		   .append(THEN).append(then);
	}
}
