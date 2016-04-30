package sqlartan.core.ast;

import sqlartan.core.ast.token.TokenSource;
import static sqlartan.core.ast.token.Keyword.RECURSIVE;
import static sqlartan.core.ast.token.Keyword.WITH;

public class SelectStatement extends Statement {
	public static SelectStatement parse(TokenSource source) {
		source.save();
		if (source.consume(WITH)) {
			source.consume(RECURSIVE);

		}
		throw new UnsupportedOperationException();
	}
}
