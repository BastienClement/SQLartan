package sqlartan.core.ast.token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenSource {
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
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

	public final List<Token> tokens;

	private TokenSource(List<Token> tokens) {
		this.tokens = Collections.unmodifiableList(tokens);
	}
}
