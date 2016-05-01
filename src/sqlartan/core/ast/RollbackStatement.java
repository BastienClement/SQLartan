package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface RollbackStatement extends Statement {
	static RollbackStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
