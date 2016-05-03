package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class ColumnDefinition implements Node {
	public static ColumnDefinition parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
