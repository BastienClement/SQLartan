package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public interface ColumnDefinition extends Node {
	static ColumnDefinition parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
