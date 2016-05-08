package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

/**
 * https://www.sqlite.org/lang_insert.html
 */
public abstract class InsertStatement implements Statement {
	public static InsertStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
