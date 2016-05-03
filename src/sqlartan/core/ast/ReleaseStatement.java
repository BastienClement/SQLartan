package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class ReleaseStatement implements Statement {
	public static Statement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
