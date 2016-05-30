package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

/**
 * https://www.sqlite.org/lang_createvtab.html
 */
public class CreateVirtualTableStatement extends CreateStatement {
	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static CreateVirtualTableStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
