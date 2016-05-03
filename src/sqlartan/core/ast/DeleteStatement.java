package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class DeleteStatement implements Statement {
	public static DeleteStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
