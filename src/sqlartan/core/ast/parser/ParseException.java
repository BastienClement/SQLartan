package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Tokenizable;

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

	public static FastParseException UnexpectedCurrentToken(Tokenizable<?>... expected) {
		return new FastParseException() {
			public ParseException materialize(ParserContext context) {
				return new UnexpectedTokenException(context.current(), expected);
			}
		};
	}

	public static final FastParseException UnexpectedNextToken = new FastParseException() {
		public ParseException materialize(ParserContext context) {
			return new UnexpectedTokenException(context.next());
		}
	};

	public static FastParseException UnexpectedNextToken(Tokenizable<?>... expected) {
		return new FastParseException() {
			public ParseException materialize(ParserContext context) {
				return new UnexpectedTokenException(context.next(), expected);
			}
		};
	}

	public static final FastParseException InvalidValuesSet = new FastParseException() {
		public ParseException materialize(ParserContext context) {
			return new ParseException("Invalid VALUES set", context.current().source, context.current().offset);
		}
	};
}
