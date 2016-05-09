package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.WHERE;

public class WhereClause implements Node {
	public Expression expression;

	public static WhereClause parse(ParserContext context) {
		WhereClause where = new WhereClause();
		context.consume(WHERE);
		where.expression = Expression.parse(context);
		return where;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(WHERE).append(expression);
	}
}
