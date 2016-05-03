package sqlartan.core.ast.token;

import sqlartan.util.Nothing;

/**
 * End of the token stream
 */
public class EndOfStream extends Token<Nothing> {
	private EndOfStream(String source, int offset) {
		super(TokenType.END_OF_STREAM, source, offset, null);
	}

	/**
	 * Constructs a EndOfStream token
	 *
	 * @param source the token source code
	 * @param offset the token offset in the source code
	 */
	public static EndOfStream at(String source, int offset) {
		return new EndOfStream(source, offset);
	}

	/**
	 * A default instance of the EOS token
	 */
	public static final EndOfStream EOS = new EndOfStream("", -1);
}
