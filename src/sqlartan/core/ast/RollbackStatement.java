package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.*;

/**
 * https://www.sqlite.org/lang_transaction.html
 */
public class RollbackStatement implements Statement {
	public String savepoint;

	public static RollbackStatement parse(ParserContext context) {
		context.consume(ROLLBACK);
		context.tryConsume(TRANSACTION);

		RollbackStatement rollback = new RollbackStatement();

		if (context.tryConsume(TO)) {
			context.tryConsume(SAVEPOINT);
			rollback.savepoint = context.consumeIdentifier().value;
		}

		return rollback;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append("ROLLBACK");
		if (savepoint != null) {
			sql.append(" TO ").append(savepoint);
		}
	}
}
