package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

/**
 * https://www.sqlite.org/lang_createtrigger.html
 */
public class CreateTriggerStatement extends CreateStatement {
	public static CreateTriggerStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
