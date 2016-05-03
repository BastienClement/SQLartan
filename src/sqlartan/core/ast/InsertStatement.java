package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class InsertStatement implements Statement {
	public static InsertStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
