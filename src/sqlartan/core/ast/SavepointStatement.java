package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class SavepointStatement implements Statement {
	public static SavepointStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
