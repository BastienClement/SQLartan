package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

/**
 * https://www.sqlite.org/lang_createview.html
 */
public class CreateViewStatement extends CreateStatement {
	public static CreateViewStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
