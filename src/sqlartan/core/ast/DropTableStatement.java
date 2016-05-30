package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.TABLE;

/**
 * https://www.sqlite.org/lang_droptable.html
 */
public class DropTableStatement extends DropStatement {
	public String table;

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static DropTableStatement parse(ParserContext context) {
		return parseDrop(context, TABLE, DropTableStatement::new, drop -> drop.table = context.consumeIdentifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		super.toSQL(sql);
		sql.appendIdentifier(table);
	}
}
