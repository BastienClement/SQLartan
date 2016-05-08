package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import java.util.List;
import static sqlartan.core.ast.Keyword.VALUES;
import static sqlartan.core.ast.Operator.LEFT_PAREN;
import static sqlartan.core.ast.Operator.RIGHT_PAREN;

public class ValuesStatement extends SelectStatement.Core {
	public List<List<Expression>> values;

	public static ValuesStatement parse(ParserContext context) {
		ValuesStatement values = new ValuesStatement();
		context.consume(VALUES);
		values.values = context.parseList(ctx -> {
			ctx.consume(LEFT_PAREN);
			List<Expression> expressions = ctx.parseList(Expression::parse);
			ctx.consume(RIGHT_PAREN);
			return expressions;
		});
		int cardinality = values.values.get(0).size();
		if (!values.values.stream().allMatch(v -> v.size() == cardinality)) {
			throw ParseException.InvalidValuesSet;
		}
		return values;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(VALUES)
		   .append(values, expressions -> ctx -> ctx.append(LEFT_PAREN).append(expressions).append(RIGHT_PAREN));
	}
}
