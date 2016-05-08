package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

/**
 * https://www.sqlite.org/lang_update.html
 */
public abstract class UpdateStatement implements Statement {
	public static UpdateStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
