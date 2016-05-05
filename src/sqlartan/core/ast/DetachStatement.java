package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.*;

/**
 * https://www.sqlite.org/lang_detach.html
 */
public class DetachStatement implements Statement {
	public String schema;

	public static DetachStatement parse(ParserContext context) {
		context.consume(DETACH);
		context.tryConsume(DATABASE);
		DetachStatement detach = new DetachStatement();
		detach.schema = context.consumeIdentifier().value;
		return detach;
	}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append("DETACH ").appendIdentifier(schema);
	}
}
