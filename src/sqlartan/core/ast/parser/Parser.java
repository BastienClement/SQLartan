package sqlartan.core.ast.parser;

import sqlartan.core.ast.Node;

/**
 * A parser function returning a Node instance from a ParserContext.
 *
 * @param <T> the type of Node returned by the function
 */
@FunctionalInterface
public interface Parser<T extends Node> {
	T parse(ParserContext context) throws FastParseException;
}
