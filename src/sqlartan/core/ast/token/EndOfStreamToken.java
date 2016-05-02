package sqlartan.core.ast.token;

import sqlartan.util.Nothing;

public class EndOfStreamToken extends Token<Nothing> {
	private EndOfStreamToken(String source, int offset) {
		super(TokenType.END_OF_STREAM, source, offset, null);
	}

	public static EndOfStreamToken at(String source, int offset) {
		return new EndOfStreamToken(source, offset);
	}

	public static final EndOfStreamToken EOS = new EndOfStreamToken("", -1);
}
