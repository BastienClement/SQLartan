package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.SAVEPOINT;

/**
 * https://www.sqlite.org/lang_savepoint.html
 */
public class SavepointStatement implements Statement {
	public String savepoint;

	public SavepointStatement() {}
	public SavepointStatement(String savepoint) {
		this.savepoint = savepoint;
	}

	public static SavepointStatement parse(ParserContext context) {
		context.consume(SAVEPOINT);
		return new SavepointStatement(context.consumeIdentifier().value);
	}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append("SAVEPOINT ").appendIdentifier(savepoint);
	}
}
