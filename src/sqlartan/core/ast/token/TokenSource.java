package sqlartan.core.ast.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * A tokens stream with transactional capabilities.
 *
 * Sources are immutable collection of tokens and must be built using the
 * TokenSource.Builder helper class.
 */
public class TokenSource {
	/**
	 * Returns a new token source builder.
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * A TokenSource builder.
	 */
	static class Builder {
		/**
		 * The list of tokens for the source
		 */
		private List<Token> tokens = new ArrayList<>();

		/**
		 * The last pushed token
		 */
		private Token last = null;

		/**
		 * Pushes a token at the end of the token list.
		 *
		 * @param token the token to push
		 */
		public void push(Token token) {
			tokens.add(token);
			last = token;
		}

		/**
		 * Returns the last pushed token.
		 */
		public Token last() {
			return last;
		}

		/**
		 * Removes the last pushed token.
		 */
		public void pop() {
			tokens.remove(tokens.size() - 1);
			last = tokens.get(tokens.size() - 1);
		}

		/**
		 * Builds a TokenSource using the pushed tokens.
		 */
		public TokenSource build() {
			return new TokenSource(tokens.toArray(new Token[0]));
		}
	}

	/**
	 * Constructs a TokenSource from the given SQL query.
	 *
	 * @param sql the SQL query to use
	 * @throws TokenizeException if the sql query is invalid
	 */
	public static TokenSource from(String sql) throws TokenizeException {
		return Tokenizer.tokenize(sql);
	}

	/**
	 * The list of tokens in this source
	 */
	private final Token[] tokens;

	/**
	 * The total length of this source
	 */
	private final int length;

	/**
	 * A reference to the EndOfStream token
	 */
	private final Token.EndOfStream eos;

	/**
	 * The current token in the source
	 */
	private Token current;

	/**
	 * The next token in the source
	 */
	private Token next;

	/**
	 * The current location in the token list
	 */
	private int cursor;

	/**
	 * The stack of markers for transactional consumption
	 */
	private Stack<Integer> marks = new Stack<>();

	/**
	 * @param tokens the array of token to use as source
	 */
	private TokenSource(Token[] tokens) {
		this.tokens = tokens;
		this.length = tokens.length;
		this.eos = (Token.EndOfStream) this.tokens[length - 1];
		setCursor(0);
	}

	/**
	 * Safely returns the token at the requested index, returning EndOfStream
	 * if the index is after the last token.
	 *
	 * @param idx the index to access
	 */
	private Token safeget(int idx) {
		return idx < length ? tokens[idx] : eos;
	}

	/**
	 * Set the internal cursor of the source at the given location.
	 *
	 * @param pos the offset to which the cursor must be moved
	 */
	private void setCursor(int pos) {
		cursor = pos;
		current = tokens[cursor];
		next = safeget(cursor + 1);
	}

	/**
	 * Consumes the current token and move the source cursor one token forward.
	 *
	 * @throws IllegalStateException if consume() is called and the current
	 * token is EndOfStream.
	 */
	public void consume() {
		if (current instanceof Token.EndOfStream) {
			throw new IllegalStateException("Attempted to consume EndOfStream token");
		}

		++cursor;
		current = next;
		next = safeget(cursor + 1);
	}

	/**
	 * Begins a transactional consumption of the source.
	 */
	public void begin() {
		marks.push(cursor);
	}

	/**
	 * Commits the current transactional consumption.
	 * @return the offset of the corresponding begin() call
	 */
	public int commit() {
		if (marks.empty()) throw new IllegalStateException();
		return marks.pop();
	}

	/**
	 * Rollbacks the source to the state at the time of the begin.
	 */
	public void rollback() {
		int target = commit();
		if (target != cursor) {
			setCursor(target);
		}
	}

	/**
	 * Returns whether a transactional consumption is open or not.
	 */
	public boolean inTransaction() {
		return !marks.empty();
	}

	/**
	 * Returns the current token.
	 */
	public Token current() {
		return current;
	}

	/**
	 * Returns the next token.
	 */
	public Token next() {
		return next;
	}

	/**
	 * Returns a list of the tokens in this source.
	 */
	public List<Token> tokens() {
		return Arrays.asList(tokens);
	}
}
