package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.TRIGGER;

/**
 * https://www.sqlite.org/lang_droptrigger.html
 */
@SuppressWarnings("WeakerAccess")
public class DropTriggerStatement extends DropStatement {
	public String trigger;

	public static DropTriggerStatement parse(ParserContext context) {
		return parseDrop(context, TRIGGER, DropTriggerStatement::new, drop -> drop.trigger = context.consumeIdentifier());
	}

	@Override
	public void toSQL(Builder sql) {
		super.toSQL(sql);
		sql.appendIdentifier(trigger);
	}
}
