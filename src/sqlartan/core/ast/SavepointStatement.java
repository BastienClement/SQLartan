package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface SavepointStatement extends Statement {
	static SavepointStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
