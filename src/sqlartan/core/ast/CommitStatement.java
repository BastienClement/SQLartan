package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface CommitStatement extends Statement {
	static CommitStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
