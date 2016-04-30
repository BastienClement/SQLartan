package sqlartan.core.ast.parser;

import sqlartan.core.ast.Node;
import sqlartan.core.ast.token.TokenSource;
import java.util.Optional;
import static sqlartan.core.ast.token.EndOfStream.EOS;

public final class ParserBuilder<T extends Node> {
	private final Parser<T> parser;

	public ParserBuilder(Parser<T> parser) {
		this.parser = parser;
	}

	public T parse(String sql) throws ParseException {
		TokenSource source = TokenSource.from(sql);
		ParserContext context = new ParserContext(source);
		try {
			T res = context.parse(parser);
			if (!context.current().equals(EOS)) {
				throw ParseException.UnexpectedCurrentToken;
			}
			return res;
		} catch (FastParseException e) {
			throw e.materialize(context);
		}
	}

	public Optional<T> tryParse(String sql) {
		try {
			return Optional.of(parse(sql));
		} catch (ParseException e) {
			return Optional.empty();
		}
	}
}
