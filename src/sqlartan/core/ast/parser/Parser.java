package sqlartan.core.ast.parser;

/**
 * A parser function returning a Node instance from a ParserContext.
 *
 * @param <T> the type of Node returned by the function
 */
@FunctionalInterface
public interface Parser<T> {
	T parse(ParserContext context) throws FastParseException;
}
