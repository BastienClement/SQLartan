package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

/**
 * https://www.sqlite.org/lang_createtable.html
 */
public class CreateTableStatement extends CreateStatement {
	public static CreateTableStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
