package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface BeginStatement extends Statement {
	static BeginStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
