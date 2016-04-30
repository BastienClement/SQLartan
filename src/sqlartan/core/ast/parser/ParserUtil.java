package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ParserUtil {
	public static <T> List<T> parseList(TokenSource source, Token separator, Supplier<T> parser) {
		List<T> list = new ArrayList<>();
		parseList(list, source, separator, parser);
		return list;
	}

	public static <T> void parseList(List<T> list, TokenSource source, Token separator, Supplier<T> parser) {
		do {
			T item = parser.get();
			if (item == null) break;
			list.add(item);
		} while (source.consume(separator));
	}
}
