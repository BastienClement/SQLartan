package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.SAVEPOINT;

/**
 * https://www.sqlite.org/lang_savepoint.html
 */
@SuppressWarnings("WeakerAccess")
public class SavepointStatement implements Statement {
	public String savepoint;

	public SavepointStatement() {}
	public SavepointStatement(String savepoint) {
		this.savepoint = savepoint;
	}

	public static SavepointStatement parse(ParserContext context) {
		context.consume(SAVEPOINT);
		return new SavepointStatement(context.consumeIdentifier());
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(SAVEPOINT).appendIdentifier(savepoint);
	}
}
