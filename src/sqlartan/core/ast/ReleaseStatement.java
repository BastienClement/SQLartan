package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.RELEASE;
import static sqlartan.core.ast.token.Keyword.SAVEPOINT;

/**
 * https://www.sqlite.org/lang_savepoint.html
 */
public class ReleaseStatement implements Statement {
	public String savepoint;

	public ReleaseStatement() {}
	public ReleaseStatement(String savepoint) {
		this.savepoint = savepoint;
	}

	public static Statement parse(ParserContext context) {
		context.consume(RELEASE);
		context.tryConsume(SAVEPOINT);
		return new ReleaseStatement(context.consumeIdentifier());
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append("RELEASE ").appendIdentifier(savepoint);
	}
}
