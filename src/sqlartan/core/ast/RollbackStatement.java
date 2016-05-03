package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class RollbackStatement implements Statement {
	public static RollbackStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
