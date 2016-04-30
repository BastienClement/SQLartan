package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class DropStatement extends Statement {
	public static DropStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
