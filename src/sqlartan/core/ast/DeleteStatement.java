package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface DeleteStatement extends Statement {
	static DeleteStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
