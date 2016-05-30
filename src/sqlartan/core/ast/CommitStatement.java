package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_transaction.html
 */
@SuppressWarnings("WeakerAccess")
public class CommitStatement implements Statement {
	public static final CommitStatement instance = new CommitStatement();

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static CommitStatement parse(ParserContext context) {
		if (!context.tryConsume(COMMIT)) {
			context.consume(END);
		}
		context.tryConsume(TRANSACTION);
		return instance;
	}

	private CommitStatement() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(COMMIT);
	}
}
