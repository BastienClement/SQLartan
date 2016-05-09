package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import java.util.Optional;

/**
 * A parser function returning a Node instance from a ParserContext.
 *
 * @param <T> the type of Node returned by the function
 */
@FunctionalInterface
public interface Parser<T> {
	T parse(ParserContext context) throws FastParseException;

	static <T> T parse(String sql, Parser<T> parser) throws ParseException {
		TokenSource source = TokenSource.from(sql);
		ParserContext context = new ParserContext(source);
		try {
			T res = parser.parse(context);
			if (!(context.current() instanceof Token.EndOfStream)) {
				throw ParseException.UnexpectedCurrentToken;
			}
			return res;
		} catch (FastParseException e) {
			throw e.materialize(context);
		}
	}

	static <T> Optional<T> tryParse(String sql, Parser<T> parser) {
		try {
			return Optional.of(parse(sql, parser));
		} catch (ParseException e) {
			return Optional.empty();
		}
	}
}
