package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface ReindexStatement extends Statement {
	static ReindexStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
