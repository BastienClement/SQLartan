package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.INDEX;

/**
 * https://www.sqlite.org/lang_dropindex.html
 */
public class DropIndexStatement extends DropStatement {
	public String index;

	public static DropIndexStatement parse(ParserContext context) {
		return parseDrop(context, INDEX, DropIndexStatement::new, drop -> drop.index = context.consumeIdentifier());
	}

	@Override
	public void toSQL(Builder sql) {
		super.toSQL(sql);
		sql.appendIdentifier(index);
	}
}
