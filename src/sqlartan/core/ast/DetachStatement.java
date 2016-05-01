package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface DetachStatement extends Statement {
	static DetachStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
