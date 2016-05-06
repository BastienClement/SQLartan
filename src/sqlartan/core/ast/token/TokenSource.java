package sqlartan.core.ast.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

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
			return new TokenSource(tokens.toArray(new Token[0]));
		}
	}

	public static TokenSource from(String sql) throws TokenizeException {
		return Tokenizer.tokenize(sql);
	}

	private final Token[] tokens;
	private final int length;
	private final Token.EndOfStream eos;

	private Token current, next;
	private int cursor;
	private Stack<Integer> marks = new Stack<>();

	private TokenSource(Token[] tokens) {
		this.tokens = tokens;
		this.length = tokens.length;
		this.eos = (Token.EndOfStream) this.tokens[length - 1];
		setCursor(0);
	}

	private Token safeget(int idx) {
		return idx < length ? tokens[idx] : eos;
	}

	private void setCursor(int pos) {
		cursor = pos;
		current = tokens[cursor];
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
		int target = commit();
		if (target != cursor) {
			setCursor(target);
		}
	}

	public Token current() {
		return current;
	}

	public Token next() {
		return next;
	}

	public List<Token> tokens() {
		return Arrays.asList(tokens);
	}
}
