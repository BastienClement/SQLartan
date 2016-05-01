package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface InsertStatement extends Statement {
	static InsertStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
