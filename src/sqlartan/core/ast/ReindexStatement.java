package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class ReindexStatement implements Statement {
	public static ReindexStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
