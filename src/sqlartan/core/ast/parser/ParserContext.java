package sqlartan.core.ast.parser;

import sqlartan.core.ast.Keyword;
import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import sqlartan.core.ast.token.Tokenizable;
import sqlartan.util.Matching;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import static sqlartan.core.ast.Operator.COMMA;
import static sqlartan.core.ast.Operator.DOT;

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
	public ParserContext(TokenSource source) {
		this.source = source;
	}

	/**
	 * Checks if a token matches the requested class
	 *
	 * @param token      the token to test
	 * @param tokenClass the class to check
	 */
	public boolean match(Tokenizable<?> token, Class<? extends Token> tokenClass) {
		return tokenClass.isAssignableFrom(token.token().getClass());
	}

	/**
	 * Checks if a token matches another one
	 *
	 * @param token the token to test
	 * @param other the other token to test
	 */
	public boolean match(Tokenizable<?> token, Tokenizable<?> other) {
		return token.token().equals(other.token());
	}

	/**
	 * Returns the current token
	 */
	public Token current() {
		return source.current();
	}

	/**
	 * Checks if the current token matches the given class
	 *
	 * @param token the token class to check
	 */
	public boolean current(Class<? extends Token> token) {
		return match(current(), token);
	}

	/**
	 * Checks if the current token matches the given token
	 *
	 * @param token the token to check
	 */
	public boolean current(Tokenizable<?> token) {
		System.out.println(current());return match(current(), token);
	}

	/**
	 * Returns the next token
	 */
	public Token next() {
		return source.next();
	}

	/**
	 * Checks if the next token matches the given class
	 *
	 * @param token the token class to check
	 */
	public boolean next(Class<? extends Token> token) {
		return match(next(), token);
	}

	/**
	 * Checks if the next token matches the given token
	 *
	 * @param token the token to check
	 */
	public boolean next(Tokenizable<?> token) {
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
	 * Resets the current transaction by moving the restore point to the current position
	 */
	public void reset() {
		source.commit();
		source.begin();
	}

	/**
	 * Consumes a token and cast it to the requested type
	 * This operation is unsafe and the actual type of the token is not checked
	 *
	 * @param <T> the type of the token to return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Token> T unsafeConsume() {
		T consumed = (T) source.current();
		source.consume();
		return consumed;
	}

	/**
	 * Unconditionally consumes a token
	 */
	public void consume() {
		source.consume();
	}

	/**
	 * Consumes a token of the given class
	 *
	 * @param token the class of the token to consume
	 * @param <T>   the type of the token to consume
	 * @return the consumed token
	 */
	public <T extends Token> T consume(Class<T> token) {
		return optConsume(token).orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	/**
	 * Consumes a token equals to the given token
	 *
	 * @param token the token to consume
	 * @param <T>   the type of the token to consume
	 * @return the consumed token
	 */
	public <T extends Token, S extends Tokenizable<T>> T consume(S token) {
		return optConsume(token).orElseThrow(ParseException.UnexpectedCurrentToken(token));
	}

	/**
	 * Consumes a chain of tokens
	 *
	 * @param tokens the tokens to consume
	 */
	public void consume(Tokenizable<?>... tokens) {
		for (Tokenizable<?> token : tokens) consume(token);
	}

	/**
	 * Consumes an identifier token
	 * If a Literal.Text token is encountered instead, it will be transformed to a identifier
	 */
	public String consumeIdentifier() {
		return optConsumeIdentifier().orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	/**
	 * Consumes a string literal token
	 * If an Identifier token is encountered instead, it will be transformed to a Literal.Text
	 */
	public String consumeTextLiteral() {
		return optConsumeTextLiteral().orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	/**
	 * Attempts to consume a token of the given class
	 *
	 * @param token the class of the token to consume
	 * @param <T>   the type of the token to consume
	 * @return true if a matching token was consumed, false otherwise
	 */
	public <T extends Token> boolean tryConsume(Class<T> token) {
		return optConsume(token).isPresent();
	}

	/**
	 * Attempts to consume a token equals to the given token
	 *
	 * @param token the token to consume
	 * @return true if a matching token was consumed, false otherwise
	 */
	public boolean tryConsume(Tokenizable<?> token) {
		return optConsume(token).isPresent();
	}

	/**
	 * Attempts to consume a chain of tokens.
	 *
	 * If the first token is a match, then every following tokens must also be a match
	 * and tryConsume() returns true.
	 *
	 * If the first token is not a match, the next tokens are not consumed and tryConsume()
	 * returns false.
	 *
	 * @param first the first token to consume
	 * @param nexts the tokens to consume if the first was a match
	 * @return true if a matching token was consumed, false otherwise
	 */
	public boolean tryConsume(Tokenizable<?> first, Tokenizable<?>... nexts) {
		if (!optConsume(first).isPresent()) return false;
		for (Tokenizable<?> token : nexts) consume(token);
		return true;
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
	public <T extends Token> Optional<T> optConsume(Class<T> token) {
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
	public <T extends Token, S extends Tokenizable<T>> Optional<T> optConsume(S token) {
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
	public Optional<String> optConsumeIdentifier() {
		Optional<Token.Identifier> res =
			Matching.match(current())
			        .when(Token.Identifier.class, id -> id)
			        .when(Token.Literal.Text.class, Token.Literal.Text::toIdentifier)
			        .get();

		Matching.match(current())
		        .when(Token.Identifier.class, id -> {System.out.println(id); return id;})
		        .when(Token.Literal.Text.class, Token.Literal.Text::toIdentifier)
		        .get();
		if (res.isPresent()) source.consume();
		return res.map(Token.Identifier::value);
	}

	/**
	 * Optionally consumes a string literal
	 * If an Identifier token is encountered instead, it will be transformed to a Literal.Text
	 *
	 * @return an optional containing a matching token, if any
	 */
	public Optional<String> optConsumeTextLiteral() {
		Optional<Token.Literal.Text> res =
			Matching.match(current())
			        .when(Token.Literal.Text.class, t -> t)
			        .when(Token.Identifier.class, id -> !id.strict, Token.Identifier::toLiteral)
			        .get();
		if (res.isPresent()) source.consume();
		return res.map(Token.Literal.Text::value);
	}

	/**
	 * Optionally consumes schema name prefix
	 */
	public Optional<String> optConsumeSchema() {
		if (next(DOT)) {
			String schema = consumeIdentifier();
			consume(DOT);
			return Optional.of(schema);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Attempts to execute another parser procedure
	 *
	 * @param parser the parser procedure to execute
	 * @param <N>    the type of node produced by the parser
	 * @return an optional containing the produced node, an empty optional in case of failure
	 */
	public <N> Optional<N> optParse(Parser<N> parser) {
		return transactionally(() -> parser.parse(this));
	}

	/**
	 * Parses a list of nodes
	 *
	 * @param separator the separator between nodes
	 * @param parser    the parser producing list items
	 * @param <N>       the type of node produced by the parser
	 * @return a list of nodes produced by the parser
	 */
	public <N> List<N> parseList(Tokenizable<?> separator, Parser<N> parser) {
		List<N> list = new ArrayList<>();
		if (!parseList(list, separator, parser)) {
			throw ParseException.UnexpectedCurrentToken;
		}
		return list;
	}

	public <N> List<N> parseList(Parser<N> parser) {
		return parseList(COMMA, parser);
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
	public <N> boolean parseList(List<N> list, Tokenizable<?> separator, Parser<N> parser) {
		try {
			begin();
			do {
				Optional<N> item = optParse(parser);
				if (item.isPresent()) {
					reset();
					list.add(item.get());
				} else {
					break;
				}
			} while (separator == Keyword.VOID || tryConsume(separator));
		} finally {
			rollback();
		}
		return !list.isEmpty();
	}

	public <N> boolean parseList(List<N> list, Parser<N> parser) {
		return parseList(list, COMMA, parser);
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
			Optional<T> res = transactionally(item);
			if (res.isPresent()) {
				return res.get();
			}
		}
		throw ParseException.UnexpectedCurrentToken;
	}

	/**
	 * Selects the first match from the supplied list of parsers
	 *
	 * @param cases a list of parsers
	 * @param <T>   the type of the returned value
	 */
	@SafeVarargs
	public final <T> T alternatives(Parser<? extends T>... cases) {
		for (Parser<? extends T> parser : cases) {
			Optional<? extends T> res = optParse(parser);
			if (res.isPresent()) {
				return res.get();
			}
		}
		throw ParseException.UnexpectedCurrentToken;
	}

	/**
	 * Transactionally executes a supplier function
	 * If a FastParseException occurs while executing the block, the token source will be rolled back
	 * to its state at the beginning of the block.
	 *
	 * @param block the function to execute
	 * @param <T>   the return type of the function
	 */
	public <T> Optional<T> transactionally(Supplier<? extends T> block) {
		begin();
		try {
			T res = block.get();
			commit();
			return Optional.of(res);
		} catch (FastParseException e) {
			rollback();
			return Optional.empty();
		} catch (Throwable t) {
			source.rollback();
			throw t;
		}
	}


	/**
	 * Binds a Parser to this ParserContext.
	 * Each call of the returned Supplier object will execute the parser on this
	 * context and return the result.
	 *
	 * @param parser  the parser to bind
	 * @param <T>     the type of elements returned by the parser
	 */
	public <T> Supplier<T> bind(Parser<T> parser) {
		return () -> parser.parse(this);
	}
}
