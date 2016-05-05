package sqlartan.core.ast;


import sqlartan.core.ast.parser.ParserContext;

/**
 * https://www.sqlite.org/lang_createindex.html
 */
public class CreateIndexStatement extends CreateStatement {
	public static CreateIndexStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
