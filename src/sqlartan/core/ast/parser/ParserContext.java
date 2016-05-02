package sqlartan.core.ast.parser;

import sqlartan.core.ast.Node;
import sqlartan.core.ast.token.IdentifierToken;
import sqlartan.core.ast.token.LiteralToken;
import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import sqlartan.util.Matching;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A parsing context
 * Keep state between parsers functions
 */
public class ParserContext {
	/**
	 * The TokenSource used
	 */
	private final TokenSource source;

	/**
	 * Constructs a new parsing context using the given token source
	 *
	 * @param source the token source to use
	 */
	ParserContext(TokenSource source) {
		this.source = source;
	}

	/**
	 * Checks if a token matches the requested class
	 *
	 * @param token      the token to test
	 * @param tokenClass the class to check
	 */
	public boolean match(Token<?> token, Class<? extends Token<?>> tokenClass) {
		return tokenClass.isAssignableFrom(token.getClass());
	}

	/**
	 * Checks if a token matches another one
	 *
	 * @param token the token to test
	 * @param other the other token to test
	 */
	public boolean match(Token<?> token, Token<?> other) {
		return token.equals(other);
	}

	/**
	 * Returns the current token
	 */
	public Token<?> current() {
		return source.current();
	}

	public boolean current(Class<? extends Token<?>> token) {
		return match(current(), token);
	}

	public boolean current(Token token) {
		return match(current(), token);
	}

	/**
	 * Returns the next token
	 */
	public Token<?> next() {
		return source.next();
	}

	public boolean next(Class<? extends Token<?>> token) {
		return match(next(), token);
	}

	public boolean next(Token<?> token) {
		return match(next(), token);
	}

	/**
	 * Begin a transactional consumption of the token source
	 * The current state of the source will be saved and can be later restored
	 */
	public void begin() {
		source.begin();
	}

	/**
	 * Commits the current consumption of the token source
	 * Remove the marker created by begin()
	 */
	public void commit() {
		source.commit();
	}

	/**
	 * Rollbacks the token source to the last marker created with begin()
	 */
	public void rollback() {
		source.rollback();
	}

	/**
	 * Consumes a token and cast it to the requested type
	 * This operation is unsafe and the actual type of the token is not checked
	 *
	 * @param <T> the type of the token to return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Token<?>> T unsafeConsume() {
		T consumed = (T) source.current();
		source.consume();
		return consumed;
	}

	/**
	 * Consumes a token of the given class
	 *
	 * @param token the class of the token to consume
	 * @param <T>   the type of the token to consume
	 * @return the consumed token
	 */
	public <T extends Token<?>> T consume(Class<T> token) {
		return optConsume(token).orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	/**
	 * Consumes a token equals to the given token
	 *
	 * @param token the token to consume
	 * @param <T>   the type of the token to consume
	 * @return the consumed token
	 */
	public <T extends Token<?>> T consume(T token) {
		return optConsume(token).orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	/**
	 * Consumes an identifier token
	 * If a Literal.Text token is encountered instead, it will be transformed to a identifier
	 */
	public IdentifierToken consumeIdentifier() {
		return optConsumeIdentifier().orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	/**
	 * Consumes a string literal token
	 * If an Identifier token is encountered instead, it will be transformed to a Literal.Text
	 */
	public LiteralToken.Text consumeTextLiteral() {
		return optConsumeTextLiteral().orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	/**
	 * Attempts to consume a token of the given class
	 *
	 * @param token the class of the token to consume
	 * @param <T>   the type of the token to consume
	 * @return true if a matching token was consumed, false otherwise
	 */
	public <T extends Token<?>> boolean tryConsume(Class<T> token) {
		return optConsume(token).isPresent();
	}

	/**
	 * Attempts to consume a token equals to the given token
	 *
	 * @param token the token to consume
	 * @param <T>   the type of the token to consume
	 * @return true if a matching token was consumed, false otherwise
	 */
	public <T extends Token<?>> boolean tryConsume(T token) {
		return optConsume(token).isPresent();
	}

	/**
	 * Attempts to consume an identifier
	 * If a Literal.Text token is encountered instead, it will be transformed to a identifier
	 *
	 * @return true if an identifier was consumed, false otherwise
	 */
	public boolean tryConsumeIdentifier() {
		return optConsumeIdentifier().isPresent();
	}

	/**
	 * Attempts to consume a string literal
	 * If an Identifier token is encountered instead, it will be transformed to a Literal.Text
	 *
	 * @return true if a string literal was consumed, false otherwise
	 */
	public boolean tryConsumeTextLiteral() {
		return optConsumeTextLiteral().isPresent();
	}

	/**
	 * Optionally consumes a token of the given class
	 *
	 * @param token the class of the token to consume
	 * @param <T>   the type of the token to consume
	 * @return an optional containing a matching token, if any
	 */
	public <T extends Token<?>> Optional<T> optConsume(Class<T> token) {
		if (current(token)) {
			return Optional.of(unsafeConsume());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Optionally consumes a token equals to the given token
	 *
	 * @param token the token to consume
	 * @param <T>   the type of the token to consume
	 * @return an optional containing a matching token, if any
	 */
	public <T extends Token<?>> Optional<T> optConsume(T token) {
		if (current(token)) {
			return Optional.of(unsafeConsume());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Optionally consumes an identifier
	 * If a Literal.Text token is encountered instead, it will be transformed to a identifier
	 *
	 * @return an optional containing a matching token, if any
	 */
	public Optional<IdentifierToken> optConsumeIdentifier() {
		Optional<IdentifierToken> res = Matching.match(current())
		                                        .when(IdentifierToken.class, id -> id)
		                                        .when(LiteralToken.Text.class, LiteralToken.Text::toIdentifier)
		                                        .get();
		if (res.isPresent()) source.consume();
		return res;
	}

	/**
	 * Optionally consumes a string literal
	 * If an Identifier token is encountered instead, it will be transformed to a Literal.Text
	 *
	 * @return an optional containing a matching token, if any
	 */
	public Optional<LiteralToken.Text> optConsumeTextLiteral() {
		Optional<LiteralToken.Text> res = Matching.match(current())
		                                          .when(LiteralToken.Text.class, t -> t)
		                                          .when(IdentifierToken.class, id -> !id.strict, IdentifierToken::toLiteral)
		                                          .get();
		if (res.isPresent()) source.consume();
		return res;
	}

	/**
	 * Executes another parser procedure
	 *
	 * @param parser the parser procedure to execute
	 * @param <N>    the type of node produced by the parser
	 * @return the node produced by the parser
	 */
	public <N extends Node> N parse(Parser<N> parser) {
		return parser.parse(this);
	}

	/**
	 * Attempts to execute another parser procedure
	 *
	 * @param parser the parser procedure to execute
	 * @param <N>    the type of node produced by the parser
	 * @return an optional containing the produced node, an empty optional in case of failure
	 */
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

	/**
	 * Parses a list of nodes
	 *
	 * @param separator the separator between nodes
	 * @param parser    the parser producing list items
	 * @param <N>       the type of node produced by the parser
	 * @return a list of nodes produced by the parser
	 */
	public <N extends Node> List<N> parseList(Token separator, Parser<N> parser) {
		List<N> list = new ArrayList<>();
		parseList(list, separator, parser);
		if (list.isEmpty()) {
			throw ParseException.UnexpectedCurrentToken;
		}
		return list;
	}

	/**
	 * Parses a list of nodes in the given list
	 *
	 * @param list      the list to put the nodes in
	 * @param separator the separator between nodes
	 * @param parser    the parser producing list item
	 * @param <N>       the type of node produced by the parser
	 * @return true if at least one node was produced, false if the resulting list is empty
	 */
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

	/**
	 * Selects the first match from the supplied list of cases
	 *
	 * @param cases a list of suppliers of cases
	 * @param <T>   the type of the selected value
	 */
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
