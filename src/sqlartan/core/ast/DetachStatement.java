package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class DetachStatement implements Statement {
	public static DetachStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
