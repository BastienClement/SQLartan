package sqlartan.core.ast.parser;

/**
 * A parsing exception
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public class ParseException extends Exception {
	/**
	 * The SQL source being parsed
	 */
	public final String source;

	/**
	 * The offset of the parser when the error occurred
	 */
	public final int offset;

	public ParseException(String message, String source, int offset) {
		super(message);
		this.source = source;
		this.offset = offset;
	}

	/**
	 * A generic "Unexpected current token" exception.
	 * This should be thrown to indicates that the current token could not be handled by the
	 * parser method. It will materialize to a UnexpectedTokenException with the current
	 * parser token.
	 */
	public static final FastParseException UnexpectedCurrentToken = new FastParseException() {
		public ParseException materialize(ParserContext context) {
			return new UnexpectedTokenException(context.current());
		}
	};
}
