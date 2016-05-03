package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public abstract class CommitStatement implements Statement {
	public static CommitStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
