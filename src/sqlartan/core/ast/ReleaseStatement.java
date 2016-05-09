package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.RELEASE;
import static sqlartan.core.ast.Keyword.SAVEPOINT;

/**
 * https://www.sqlite.org/lang_savepoint.html
 */
@SuppressWarnings("WeakerAccess")
public class ReleaseStatement implements Statement {
	public String savepoint;

	public static Statement parse(ParserContext context) {
		context.consume(RELEASE);
		context.tryConsume(SAVEPOINT);
		ReleaseStatement release = new ReleaseStatement();
		release.savepoint = context.consumeIdentifier();
		return release;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(RELEASE).appendIdentifier(savepoint);
	}
}
