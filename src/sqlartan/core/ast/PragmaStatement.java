package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class PragmaStatement implements Statement {
	public static PragmaStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
