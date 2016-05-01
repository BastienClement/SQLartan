package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface ReleaseStatement extends Statement {
	static Statement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
