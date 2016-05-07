package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.INDEX;

/**
 * https://www.sqlite.org/lang_droptable.html
 */
public class DropTableStatement extends DropStatement {
	public String table;

	public static DropTableStatement parse(ParserContext context) {
		return parseDrop(context, INDEX, DropTableStatement::new, drop -> drop.table = context.consumeIdentifier());
	}

	@Override
	public void toSQL(Builder sql) {
		super.toSQL(sql);
		sql.appendIdentifier(table);
	}
}
