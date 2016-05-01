package sqlartan.core.ast.parser;

import sqlartan.core.ast.Node;
import sqlartan.core.ast.token.Identifier;
import sqlartan.core.ast.token.Literal;
import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import static sqlartan.util.Matching.match;

public class ParserContext {
	private final TokenSource source;

	ParserContext(TokenSource source) {
		this.source = source;
	}

	public Token current() {
		return source.current();
	}

	public Token next() {
		return source.next();
	}

	public void begin() {
		source.begin();
	}
	public void commit() {
		source.commit();
	}
	public void rollback() {
		source.rollback();
	}

	@SuppressWarnings("unchecked")
	private <T extends Token> T unsafeConsume() {
		T consumed = (T) source.current();
		source.consume();
		return consumed;
	}

	public <T extends Token> T consume(Class<T> token) {
		return optConsume(token).orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	public <T extends Token> T consume(T token) {
		return optConsume(token).orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	public Identifier consumeIdentifier() {
		return optConsumeIdentifier().orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	public Literal.Text consumeTextLiteral() {
		return optConsumeTextLiteral().orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	public <T extends Token> boolean tryConsume(Class<T> token) {
		return optConsume(token).isPresent();
	}

	public <T extends Token> boolean tryConsume(T token) {
		return optConsume(token).isPresent();
	}

	public boolean tryConsumeIdentifier() {
		return optConsumeIdentifier().isPresent();
	}

	public boolean tryConsumeTextLiteral() {
		return optConsumeTextLiteral().isPresent();
	}

	public <T extends Token> Optional<T> optConsume(Class<T> token) {
		if (token.isAssignableFrom(current().getClass())) {
			return Optional.of(unsafeConsume());
		} else {
			return Optional.empty();
		}
	}

	public <T extends Token> Optional<T> optConsume(T token) {
		if (current().equals(token)) {
			return Optional.of(unsafeConsume());
		} else {
			return Optional.empty();
		}
	}

	public Optional<Identifier> optConsumeIdentifier() {
		Optional<Identifier> res = match(current())
			.when(Identifier.class, id -> id)
			.when(Literal.Text.class, Literal.Text::toIdentifier)
			.get();
		if (res.isPresent()) source.consume();
		return res;
	}

	public Optional<Literal.Text> optConsumeTextLiteral() {
		Optional<Literal.Text> res = match(current())
			.when(Literal.Text.class, t -> t)
			.when(Identifier.class, id -> !id.strict, Identifier::toLiteral)
			.get();
		if (res.isPresent()) source.consume();
		return res;
	}

	public <N extends Node> N parse(Parser<N> parser) {
		return parser.parse(this);
	}

	public <N extends Node> Optional<N> tryParse(Parser<N> parser) {
		source.begin();
		try {
			N res = parser.parse(this);
			source.commit();
			return Optional.of(res);
		} catch (FastParseException e) {
			source.rollback();
			return Optional.empty();
		} catch (Throwable t) {
			source.rollback();
			throw t;
		}
	}

	public <N extends Node> List<N> parseList(Token separator, Parser<N> parser) {
		List<N> list = new ArrayList<>();
		parseList(list, separator, parser);
		return list;
	}

	public <N extends Node> boolean parseList(List<N> list, Token separator, Parser<N> parser) {
		do {
			Optional<N> item = tryParse(parser);
			if (item.isPresent()) {
				list.add(item.get());
			} else {
				break;
			}
		} while (tryConsume(separator));
		return !list.isEmpty();
	}

	@SafeVarargs
	public final <T> T alternatives(Supplier<? extends T>... cases) {
		for (Supplier<? extends T> item : cases) {
			begin();
			try {
				T node = item.get();
				commit();
				return node;
			} catch (FastParseException e) {
				rollback();
			} catch (Throwable t) {
				source.rollback();
				throw t;
			}
		}
		throw ParseException.UnexpectedCurrentToken;
	}
}
