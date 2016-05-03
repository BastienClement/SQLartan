package sqlartan.core.ast.token;

import sqlartan.util.Nothing;

public class EndOfStream extends Token<Nothing> {
	private EndOfStream(String source, int offset) {
		super(TokenType.END_OF_STREAM, source, offset, null);
	}

	public static EndOfStream at(String source, int offset) {
		return new EndOfStream(source, offset);
	}

	public static final EndOfStream EOS = new EndOfStream("", -1);
}
