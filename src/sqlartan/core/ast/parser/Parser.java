package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import java.util.Optional;

/**
 * A parser function returning a T from a ParserContext.
 *
 * Each parsing function is responsible for parsing a subset of the SQL
 * language. The most general one, Statement::parse is responsible for
 * disambiguating between the different type of statement and call the
 * appropriate sub-parser function.
 *
 * This implementation also make it trivial to handle recursive cases
 * in the SQL language.
 *
 * @param <T> the type of element returned by the function
 */
@FunctionalInterface
public interface Parser<T> {
	/**
	 * Parses an instance of T out of the given ParserContext.
	 *
	 * @param context the parser context to consume
	 * @throws FastParseException if the source is invalid
	 */
	T parse(ParserContext context) throws FastParseException;

	/**
	 * Helper method for parsing SQL.
	 *
	 * The parser can be either Statement::parse or a more specific parser
	 * if the type of the query being parsed is known. If a more specific
	 * parser is given, the return type of this function is adapted accordingly.
	 *
	 * Unless the partial argument is true, the input query must be fully
	 * consumed by the parser. If the next token available in the stream of
	 * tokens is not EndOfStream, an ParseException is thrown.
	 *
	 * @param sql     the SQL query to parse
	 * @param parser  the parser to use for this query
	 * @param partial whether to allow the parser to consume the given input partially
	 * @param <T>     the type of result of the parser function
	 * @throws ParseException if the source is invalid for the given parser
	 */
	static <T> T parse(String sql, Parser<T> parser, boolean partial) throws ParseException {
		TokenSource source = TokenSource.from(sql);
		ParserContext context = new ParserContext(source);
		try {
			T res = parser.parse(context);
			if (source.inTransaction()) {
				throw new ParseException("Unterminated transactional consumption of the token source",
					source.current().source, source.current().offset);
			} else if (partial || !(context.current() instanceof Token.EndOfStream)) {
				throw ParseException.UnexpectedCurrentToken;
			}
			return res;
		} catch (FastParseException e) {
			throw e.materialize(context);
		}
	}

	/**
	 * Alias for parse() with the partial argument to false.
	 *
	 * @param sql    the SQL query t parse
	 * @param parser the parser to use for this query
	 * @param <T>    the type of result of the parser function
	 * @throws ParseException if the source is invalid for the given parser
	 */
	static <T> T parse(String sql, Parser<T> parser) throws ParseException {
		return parse(sql, parser, false);
	}

	/**
	 * Attempts to parse the input query by using the given parser.
	 * Similar to parse() but returns Optional.empty() is the source is invalid.
	 *
	 * @param sql     the SQL query to parse
	 * @param parser  the parser to use for this query
	 * @param partial whether to allow the parser to consume the given input partially
	 * @param <T>     the type of result of the parser function
	 */
	static <T> Optional<T> tryParse(String sql, Parser<T> parser, boolean partial) {
		try {
			return Optional.of(parse(sql, parser));
		} catch (ParseException e) {
			return Optional.empty();
		}
	}

	/**
	 * Alias for tryParse() with the partial argument to false.
	 *
	 * @param sql    the SQL query to parse
	 * @param parser the parser to use for this query
	 * @param <T>    the type of result of the parser function
	 */
	static <T> Optional<T> tryParse(String sql, Parser<T> parser) {
		return tryParse(sql, parser, false);
	}
}
