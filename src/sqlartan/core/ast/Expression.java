package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface Expression extends Node {
	static Expression parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
