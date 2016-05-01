package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface CreateStatement extends Statement {
	static CreateStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
