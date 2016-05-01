package sqlartan.core.ast.parser;

import sqlartan.core.ast.Node;

@FunctionalInterface
public interface Parser<T extends Node> {
	<A> T parse(ParserContext context) throws FastParseException;
}
