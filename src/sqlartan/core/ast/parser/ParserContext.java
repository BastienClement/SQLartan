package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	public <T> void consume(Class<T> token) {
		if (!tryConsume(token)) {
			throw ParseException.UnexpectedCurrentToken;
		}
	}

	public void consume(Token token) {
		if (!tryConsume(token)) {
			throw ParseException.UnexpectedCurrentToken;
		}
	}

	public <T> boolean tryConsume(Class<T> token) {
		if (token.isAssignableFrom(current().getClass())) {
			source.consume();
			return true;
		} else {
			return false;
		}
	}

	public boolean tryConsume(Token token) {
		if (current().equals(token)) {
			source.consume();
			return true;
		} else {
			return false;
		}
	}

	public <T> T parse(Parser<T> parser) {
		return parser.parse(this);
	}

	public <T> Optional<T> tryParse(Parser<T> parser) {
		source.begin();
		try {
			T res = parser.parse(this);
			source.commit();
			return Optional.of(res);
		} catch (FastParseException e) {
			source.rollback();
			return Optional.empty();
		}
	}

	public <T> List<T> parseList(Token separator, Parser<T> parser) {
		List<T> list = new ArrayList<>();
		parseList(list, separator, parser);
		return list;
	}

	public <T> void parseList(List<T> list, Token separator, Parser<T> parser) {
		do {
			Optional<T> item = tryParse(parser);
			if (item.isPresent()) {
				list.add(item.get());
			} else {
				break;
			}
		} while (tryConsume(separator));
	}
}
