package sqlartan.core.ast.token;

import sqlartan.core.ast.Node;

/**
 * Input source token
 */
public abstract class Token implements Tokenizable<Token> {
	/**
	 * The SQL source from which this token is extracted
	 */
	public final String source;

	/**
	 * The offset of this token in the source SQL query
	 */
	public final int offset;

	/**
	 * Constructs a token from a given source and at a given offset.
	 *
	 * @param source the SQL source from which this token is read
	 * @param offset the offset of the token in the source
	 */
	protected Token(String source, int offset) {
		this.source = source;
		this.offset = offset;
	}

	/**
	 * Returns the prefix to use when serializing this token to String.
	 */
	public abstract String stringPrefix();

	/**
	 * Returns the value representation to use when serializing this token
	 * to String.
	 */
	public abstract String stringValue();

	/**
	 * Returns a String representation of this Token.
	 */
	@Override
	public String toString() {
		return stringPrefix() + "(" + stringValue() + ")";
	}

	/**
	 * Implementation of the token() method from Tokenizable.
	 * Returns itself.
	 */
	@Override
	public Token token() { return this; }

	/**
	 * An abstract token with a String value
	 */
	public static abstract class StringValued extends Token {
		/**
		 * The String value of this token
		 */
		public final String value;

		/**
		 * Constructs a new StringValued token with the given value.
		 *
		 * @param value  the value of this token
		 * @param source the source SQl from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		protected StringValued(String value, String source, int offset) {
			super(source, offset);
			this.value = value;
		}

		/**
		 * Returns the value of this token.
		 */
		public String value() {
			return value;
		}

		/**
		 * Returns a string representation of the value of this token.
		 * Since the value is a String, the value itself is returned.
		 */
		@Override
		public String stringValue() { return value; }
	}

	/**
	 * An Identifier
	 */
	public static class Identifier extends StringValued {
		/**
		 * Whether this identifier was strictly defined or not
		 * Strict identifiers should not be implicitly converted to text literals.
		 */
		public final boolean strict;

		/**
		 * Constructs a new identifier token.
		 *
		 * @param value  the identifier name
		 * @param strict whether the identifier was strictly defined or not
		 *               Strict identifiers should not be implicitly converted
		 *               to text literals.
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		private Identifier(String value, boolean strict, String source, int offset) {
			super(value, source, offset);
			this.strict = strict;
		}

		/**
		 * Implicitly casts this identifier to a text literal.
		 * This function always succeeds, even if the identifier was strict.
		 */
		public Literal.Text toLiteral() {
			return Literal.fromText(value, source, offset);
		}

		@Override
		public String stringPrefix() { return "Identifier"; }

		/**
		 * Builds a new identifier token.
		 *
		 * @param identifier the identifier name
		 * @param strict     whether the identifier was strictly defined or not
		 *                   Strict identifiers should not be implicitly
		 *                   converted to text literals.
		 * @param source     the source SQL from which this token is read
		 * @param offset     the offset of the token in the source query
		 */
		public static Identifier from(String identifier, boolean strict, String source, int offset) {
			return new Identifier(identifier, strict, source, offset);
		}
	}

	/**
	 * A text or numeric literal
	 */
	public static abstract class Literal extends StringValued {
		/**
		 * @param value  the literal value of this token as String
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		private Literal(String value, String source, int offset) {
			super(value, source, offset);
		}

		@Override
		public String stringPrefix() { return "Literal"; }

		/**
		 * A text literal
		 */
		public static class Text extends Literal {
			/**
			 * @param value  the literal value of this token as String
			 * @param source the source SQL from which this token is read
			 * @param offset the offset of the token in the source query
			 */
			private Text(String value, String source, int offset) {
				super(value, source, offset);
			}

			/**
			 * Implicitly converts this text literal to an identifier.
			 */
			public Identifier toIdentifier() {
				return Identifier.from(value, false, source, offset);
			}
		}

		/**
		 * Builds a new Text literal token.
		 *
		 * @param text   the literal value of this token
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		public static Text fromText(String text, String source, int offset) {
			return new Text(text, source, offset);
		}

		/**
		 * A numeric literal
		 */
		public static class Numeric extends Literal {
			/**
			 * @param value  the literal value of this token as String
			 * @param source the source SQL from which this token is read
			 * @param offset the offset of the token in the source query
			 */
			private Numeric(String value, String source, int offset) {
				super(value, source, offset);
			}
		}

		/**
		 * Builds a new Numeric literal token.
		 *
		 * @param number the literal value of this token
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		public static Numeric fromNumeric(String number, String source, int offset) {
			return new Numeric(number, source, offset);
		}
	}

	/**
	 * An SQL placeholder
	 */
	public static abstract class Placeholder extends Token {
		/**
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		private Placeholder(String source, int offset) {
			super(source, offset);
		}

		@Override
		public String stringPrefix() { return "Placeholder"; }

		/**
		 * An indexed placeholder
		 */
		public static class Indexed extends Placeholder {
			/**
			 * The placeholder index
			 */
			public final int index;

			/**
			 * @param index  the placeholder index
			 * @param source the source SQL from which this token is read
			 * @param offset the offset of the token in the source query
			 */
			private Indexed(int index, String source, int offset) {
				super(source, offset);
				this.index = index;
			}

			@Override
			public String stringValue() { return "?" + index; }
		}

		/**
		 * Builds a new indexed placeholder token.
		 *
		 * @param index  the placeholder index
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		public static Indexed fromIndex(int index, String source, int offset) {
			return new Indexed(index, source, offset);
		}

		/**
		 * A named placeholder
		 */
		public static class Named extends Placeholder {
			/**
			 * The placeholder name
			 */
			public final String name;

			/**
			 * @param name   the placeholder name
			 * @param source the source SQL from which this token is read
			 * @param offset the offset of the token in the source query
			 */
			private Named(String name, String source, int offset) {
				super(source, offset);
				this.name = name;
			}

			@Override
			public String stringValue() { return ":" + name; }
		}

		/**
		 * Builds a new named placeholder token.
		 *
		 * @param name   the placeholder name
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		public static Named fromName(String name, String source, int offset) {
			return new Named(name, source, offset);
		}
	}

	/**
	 * A token wrapping a distinguishing value.
	 *
	 * Two Wrapper tokens are considered equals if their wrapped value
	 * are equals.
	 *
	 * @param <T> the type of the wrapper value
	 */
	public static abstract class Wrapper<T extends Node.Enumerated> extends Token {
		/**
		 * The wrapped value of this token
		 */
		public final T ref;

		/**
		 * @param ref    wrapped value of this token
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		protected Wrapper(T ref, String source, int offset) {
			super(source, offset);
			this.ref = ref;
		}

		/**
		 * Returns the wrapped value.
		 */
		public T node() { return ref; }

		/**
		 * Constructs a new Wrapper token with the same value as this one
		 * but at another location in the input query.
		 *
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		public abstract Wrapper<T> at(String source, int offset);

		/**
		 * Two Wrapper token are equals iff their wrapped value are equals.
		 *
		 * @param o the object to test for equality
		 */
		@Override
		public boolean equals(Object o) {
			return ref == o || (this.getClass().isInstance(o) && ((Wrapper) o).ref == ref);
		}

		/**
		 * Returns the hashCode of the wrapped value.
		 */
		@Override
		public int hashCode() { return ref.hashCode(); }
	}

	/**
	 * A token wrapping a SQL Keyword
	 */
	public static class Keyword extends Wrapper<sqlartan.core.ast.Keyword> {
		/**
		 * @param ref    the SQL keyword to wrap
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		private Keyword(sqlartan.core.ast.Keyword ref, String source, int offset) {
			super(ref, source, offset);
		}

		/**
		 * Constructs a new Keyword token with the same wrapped keyword.
		 *
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		@Override
		public Keyword at(String source, int offset) {
			return new Keyword(ref, source, offset);
		}

		@Override
		public String stringPrefix() { return "Keyword"; }

		@Override
		public String stringValue() { return ref.name; }

		/**
		 * Builds a dummy Keyword token without a meaningful source or
		 * offset value.
		 *
		 * @param keyword the SQL keyword to wrap
		 */
		public static Keyword dummyFor(sqlartan.core.ast.Keyword keyword) {
			return new Keyword(keyword, null, -1);
		}
	}

	/**
	 * A token wrapping a SQL Operator
	 */
	public static class Operator extends Wrapper<sqlartan.core.ast.Operator> {
		/**
		 * @param ref    the SQL operator to wrap
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		private Operator(sqlartan.core.ast.Operator ref, String source, int offset) {
			super(ref, source, offset);
		}

		/**
		 * Constructs a new Operator token with the same wrapped operator.
		 *
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		@Override
		public Operator at(String source, int offset) {
			return new Operator(ref, source, offset);
		}

		@Override
		public String stringPrefix() { return "Operator"; }

		@Override
		public String stringValue() { return ref.symbol; }

		/**
		 * Builds a dummy Operator token without a meaningful source or
		 * offset value.
		 *
		 * @param operator the SQL operator to wrap
		 */
		public static Operator dummyFor(sqlartan.core.ast.Operator operator) {
			return new Operator(operator, null, -1);
		}
	}

	/**
	 * End of stream token
	 */
	public static class EndOfStream extends Token {
		/**
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		private EndOfStream(String source, int offset) {
			super(source, offset);
		}

		@Override
		public String stringPrefix() { return "EndOfStream"; }

		@Override
		public String stringValue() { return ""; }

		/**
		 * An EOS token is always equals to another EOS token.
		 *
		 * @param o the object to test for equality
		 */
		@Override
		public boolean equals(Object o) { return o == this || o instanceof EndOfStream; }

		/**
		 * Returns the hashCode of the EndOfStream class.
		 */
		@Override
		public int hashCode() { return EndOfStream.class.hashCode(); }

		/**
		 * Builds a new EndOfStream token at the given offset in the
		 * given source.
		 *
		 * @param source the source SQL from which this token is read
		 * @param offset the offset of the token in the source query
		 */
		public static EndOfStream at(String source, int offset) {
			return new EndOfStream(source, offset);
		}
	}
}
