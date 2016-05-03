package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class AttachStatement implements Statement {
	public static AttachStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
