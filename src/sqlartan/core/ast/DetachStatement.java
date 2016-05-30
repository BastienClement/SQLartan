package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.DATABASE;
import static sqlartan.core.ast.Keyword.DETACH;

/**
 * https://www.sqlite.org/lang_detach.html
 */
public class DetachStatement implements Statement {
	public String schema;

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static DetachStatement parse(ParserContext context) {
		context.consume(DETACH);
		context.tryConsume(DATABASE);
		DetachStatement detach = new DetachStatement();
		detach.schema = context.consumeIdentifier();
		return detach;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(DETACH).appendIdentifier(schema);
	}
}
