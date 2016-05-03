package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class BeginStatement implements Statement {
	public static BeginStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
