package sqlartan.core.ast.token;

import java.util.*;

public class TokenSource {
	static Builder builder() {
		return new Builder();
	}

	static class Builder {
		private List<Token> tokens = new ArrayList<>();
		private Token last = null;

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
			return new TokenSource(tokens);
		}
	}

	public static TokenSource from(String sql) throws TokenizeException {
		return Tokenizer.tokenize(sql);
	}

	public final List<Token> tokens;
	public final int length;

	private Token current, next;
	private int cursor;
	private Stack<Integer> marks = new Stack<>();

	private TokenSource(List<Token> tokens) {
		this.tokens = Collections.unmodifiableList(tokens);
		this.length = tokens.size();
		setCursor(0);
	}

	private void setCursor(int pos) {
		cursor = pos;
		current = tokens.get(cursor);
		next = (cursor + 1 < length) ? tokens.get(cursor + 1) : null;
	}

	public Token current() {
		return current;
	}

	public Token next() {
		return next;
	}

	public void save() {
		marks.push(cursor);
	}

	public boolean consume() {
		if (current != null) {
			++cursor;
			current = next;
			next = (cursor + 1 < length) ? tokens.get(cursor + 1) : null;
			return true;
		}
		return false;
	}

	public boolean consume(Token token) {
		return current == token && consume();
	}

	public <T extends Token> boolean consume(Class<T> tokenClass) {
		return tokenClass.isAssignableFrom(current.getClass()) && consume();
	}

	public void restore() {
		if (marks.empty()) throw new IllegalStateException();
		setCursor(marks.pop());
	}
}
