package sqlartan.core.ast;

import sqlartan.core.ast.token.TokenSource;

public abstract class DropStatement extends Statement {
	public static DropStatement parse(TokenSource source) {
		throw new UnsupportedOperationException();
	}
}
