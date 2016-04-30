package sqlartan.core.ast.parser;

@SuppressWarnings("ThrowableInstanceNeverThrown")
public class ParseException extends Exception {
	public final String source;
	public final int offset;

	public ParseException(String message, String source, int offset) {
		super(message);
		this.source = source;
		this.offset = offset;
	}

	public static final FastParseException UnexpectedCurrentToken = new FastParseException() {
		public ParseException materialize(ParserContext context) {
			return new UnexpectedTokenException(context.current());
		}
	};

	public static final FastParseException UnexpectedNextToken = new FastParseException() {
		public ParseException materialize(ParserContext context) {
			return new UnexpectedTokenException(context.next());
		}
	};
}
