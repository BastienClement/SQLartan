package sqlartan.core.ast;

import sqlartan.core.ast.token.TokenSource;

public abstract class CreateStatement extends Statement {
	public static CreateStatement parse(TokenSource source) {
		throw new UnsupportedOperationException();
	}
}
