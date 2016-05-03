package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class CreateStatement implements Statement {
	public static CreateStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
