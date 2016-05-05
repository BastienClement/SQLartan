package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.COMMIT;
import static sqlartan.core.ast.token.Keyword.END;
import static sqlartan.core.ast.token.Keyword.TRANSACTION;

/**
 * https://www.sqlite.org/lang_transaction.html
 */
public class CommitStatement implements Statement {
	public static final CommitStatement instance = new CommitStatement();

	public static CommitStatement parse(ParserContext context) {
		if (!context.tryConsume(COMMIT)) {
			context.consume(END);
		}
		context.tryConsume(TRANSACTION);
		return instance;
	}

	private CommitStatement() {}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append("COMMIT");
	}
}
