package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface AttachStatement extends Statement {
	static AttachStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
