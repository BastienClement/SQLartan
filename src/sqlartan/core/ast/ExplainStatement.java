package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

public class ExplainStatement implements Statement {
	public static ExplainStatement parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}

	public Statement statement;
}
