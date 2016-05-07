package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.INDEX;

/**
 * https://www.sqlite.org/lang_dropview.html
 */
public class DropViewStatement extends DropStatement {
	public String view;

	public static DropViewStatement parse(ParserContext context) {
		return parseDrop(context, INDEX, DropViewStatement::new, drop -> drop.view = context.consumeIdentifier());
	}

	@Override
	public void toSQL(Builder sql) {
		super.toSQL(sql);
		sql.appendIdentifier(view);
	}
}
