package sqlartan.core.ast.token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class TokenSource {
	static Builder builderFor(String sql) {
		return new Builder(sql);
	}

	static class Builder {
		private final String sql;
		private List<Token> tokens = new ArrayList<>();
		private Token last = null;

		private Builder(String sql) {
			this.sql = sql;
		}

		public void push(Token token) {
			tokens.add(token);
			last = token;
		}

		public Token last() {
			return last;
		}

		public void pop() {
			tokens.remove(tokens.size() - 1);
			last = tokens.get(tokens.size() - 1);
		}

		public TokenSource build() {
			return new TokenSource(tokens, sql);
		}
	}

	public static TokenSource from(String sql) throws TokenizeException {
		return Tokenizer.tokenize(sql);
	}

	public final String sql;
	public final List<Token> tokens;
	public final int length;
	public final Token.EndOfStream eos;

	private Token current, next;
	private int cursor;
	private Stack<Integer> marks = new Stack<>();

	private TokenSource(List<Token> tokens, String sql) {
		this.sql = sql;
		this.tokens = Collections.unmodifiableList(tokens);
		this.length = tokens.size();
		this.eos = (Token.EndOfStream) tokens.get(length - 1);
		setCursor(0);
	}

	private Token safeget(int idx) {
		return idx < length ? tokens.get(idx) : eos;
	}

	private void setCursor(int pos) {
		cursor = pos;
		current = tokens.get(cursor);
		next = safeget(cursor + 1);
	}

	public void consume() {
		if (current instanceof Token.EndOfStream) {
			throw new IllegalStateException("Attempted to consume EndOfStream token");
		}

		++cursor;
		current = next;
		next = safeget(cursor + 1);
	}

	public void begin() {
		marks.push(cursor);
	}

	public int commit() {
		if (marks.empty()) throw new IllegalStateException();
		return marks.pop();
	}

	public void rollback() {
		setCursor(commit());
	}

	public Token current() {
		return current;
	}

	public Token next() {
		return next;
	}
}
