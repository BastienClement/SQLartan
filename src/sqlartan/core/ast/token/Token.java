package sqlartan.core.ast.token;

import sqlartan.core.ast.Node;

/**
 * Input source token
 */
public abstract class Token implements Tokenizable<Token> {
	public final String source;
	public final int offset;

	protected Token(String source, int offset) {
		this.source = source;
		this.offset = offset;
	}

	public abstract String stringPrefix();
	public abstract String stringValue();

	@Override
	public String toString() {
		return stringPrefix() + "(" + stringValue() + ")";
	}

	@Override
	public Token token() { return this; }

	/**
	 * An abstract token with a String value
	 */
	public static abstract class StringValued extends Token {
		public final String value;

		protected StringValued(String value, String source, int offset) {
			super(source, offset);
			this.value = value;
		}

		public String value() {
			return value;
		}

		@Override public String stringValue() { return value; }
	}

	/**
	 * Identifier
	 */
	public static class Identifier extends StringValued {
		public final boolean strict;

		private Identifier(String value, boolean strict, String source, int offset) {
			super(value, source, offset);
			this.strict = strict;
		}

		public Literal.Text toLiteral() {
			return Literal.fromText(value, source, offset);
		}

		@Override public String stringPrefix() { return "Identifier"; }

		public static Identifier from(String identifier, boolean strict, String source, int offset) {
			return new Identifier(identifier, strict, source, offset);
		}
	}

	/**
	 * Text or numeric literal
	 */
	public static abstract class Literal extends StringValued {
		private Literal(String value, String source, int offset) {
			super(value, source, offset);
		}

		@Override public String stringPrefix() { return "Literal"; }

		public static class Text extends Literal {
			private Text(String value, String source, int offset) {
				super(value, source, offset);
			}

			public Identifier toIdentifier() {
				return Identifier.from(value, false, source, offset);
			}
		}

		public static Text fromText(String text, String source, int offset) {
			return new Text(text, source, offset);
		}

		public static class Numeric extends Literal {
			private Numeric(String value, String source, int offset) {
				super(value, source, offset);
			}
		}

		public static Numeric fromNumeric(String number, String source, int offset) {
			return new Numeric(number, source, offset);
		}
	}

	/**
	 * Placeholder
	 */
	public static abstract class Placeholder extends Token {
		protected Placeholder(String source, int offset) {
			super(source, offset);
		}

		@Override public String stringPrefix() { return "Placeholder"; }

		public static class Indexed extends Placeholder {
			public final int index;

			private Indexed(int index, String source, int offset) {
				super(source, offset);
				this.index = index;
			}

			@Override
			public String stringValue() { return "?" + index; }
		}

		public static Indexed fromIndex(int index, String source, int offset) {
			return new Indexed(index, source, offset);
		}

		public static class Named extends Placeholder {
			public final String name;

			private Named(String name, String source, int offset) {
				super(source, offset);
				this.name = name;
			}

			@Override
			public String stringValue() { return ":" + name; }
		}

		public static Named fromName(String name, String source, int offset) {
			return new Named(name, source, offset);
		}
	}

	/**
	 * A token wrapping a distinguishing value
	 * @param <T> the type of the wrapper value
	 */
	public static abstract class Wrapper<T extends Node.Enumerated> extends Token {
		public final T ref;

		protected Wrapper(T ref, String source, int offset) {
			super(source, offset);
			this.ref = ref;
		}

		public T node() { return ref; }

		public abstract Wrapper<T> at(String source, int offset);

		@Override public boolean equals(Object o) {
			return ref == o || (this.getClass().isInstance(o) && ((Wrapper) o).ref == ref);
		}

		@Override public int hashCode() { return ref.hashCode(); }
	}

	/**
	 * A token wrapping a SQL Keyword
	 */
	public static class Keyword extends Wrapper<sqlartan.core.ast.Keyword> {
		private Keyword(sqlartan.core.ast.Keyword ref, String source, int offset) {
			super(ref, source, offset);
		}

		@Override
		public Keyword at(String source, int offset) {
			return new Keyword(ref, source, offset);
		}

		@Override public String stringPrefix() { return "Keyword"; }
		@Override public String stringValue() { return ref.name; }

		public static Keyword dummyFor(sqlartan.core.ast.Keyword keyword) {
			return new Keyword(keyword, null, -1);
		}
	}

	/**
	 * A token wrapping a SQL Operator
	 */
	public static class Operator extends Wrapper<sqlartan.core.ast.Operator> {
		private Operator(sqlartan.core.ast.Operator ref, String source, int offset) {
			super(ref, source, offset);
		}

		@Override
		public Operator at(String source, int offset) {
			return new Operator(ref, source, offset);
		}

		@Override public String stringPrefix() { return "Operator"; }
		@Override public String stringValue() { return ref.symbol; }

		public static Operator dummyFor(sqlartan.core.ast.Operator operator) {
			return new Operator(operator, null, -1);
		}
	}

	/**
	 * End of stream
	 */
	public static class EndOfStream extends Token {
		private EndOfStream(String source, int offset) {
			super(source, offset);
		}

		@Override public String stringPrefix() { return "EndOfStream"; }
		@Override public String stringValue() { return ""; }

		@Override public boolean equals(Object o) { return o instanceof EndOfStream; }
		@Override public int hashCode() { return EndOfStream.class.hashCode(); }

		public static EndOfStream at(String source, int offset) {
			return new EndOfStream(source, offset);
		}
	}
}
