package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface DropStatement extends Statement {
	static DropStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
