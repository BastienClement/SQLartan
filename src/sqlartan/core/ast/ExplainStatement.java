package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.*;

/**
 * https://www.sqlite.org/lang_explain.html
 */
public class ExplainStatement implements Statement {
	public Statement statement;
	public boolean queryPlan;

	public static ExplainStatement parse(ParserContext context) {
		context.consume(EXPLAIN);
		ExplainStatement explain = new ExplainStatement();
		if (context.tryConsume(QUERY)) {
			context.consume(PLAN);
			explain.queryPlan = true;
		}
		explain.statement = context.parse(Statement::parse);
		return explain;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append("EXPLAIN ");
		if (queryPlan) sql.append("QUERY PLAN ");
		sql.append(statement);
	}
}
