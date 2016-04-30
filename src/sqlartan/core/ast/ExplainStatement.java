package sqlartan.core.ast;

import sqlartan.core.ast.token.TokenSource;

public class ExplainStatement extends Statement {
	public static ExplainStatement parse(TokenSource source) {
		throw new UnsupportedOperationException();
	}

	public Statement statement;
}
