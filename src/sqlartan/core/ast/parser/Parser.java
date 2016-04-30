package sqlartan.core.ast.parser;

@FunctionalInterface
public interface Parser<T> {
	T parse(ParserContext context) throws FastParseException;
}
