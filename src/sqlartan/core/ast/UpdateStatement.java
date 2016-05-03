package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class UpdateStatement implements Statement {
	public static UpdateStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
