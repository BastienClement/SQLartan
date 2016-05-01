package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface PragmaStatement extends Statement {
	static PragmaStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
