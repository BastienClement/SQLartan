package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.ASC;
import static sqlartan.core.ast.Keyword.DESC;

/**
 * Ordering terms (ASC / DESC)
 */
public enum Ordering implements Node.Enumerated {
	None(null), Asc(ASC), Desc(DESC);

	/**
	 * The associated keyword value
	 */
	public final Keyword keyword;

	Ordering(Keyword keyword) {
		this.keyword = keyword;
	}

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static Ordering parse(ParserContext context) {
		if (context.tryConsume(ASC)) {
			return Asc;
		} else if (context.tryConsume(DESC)) {
			return Desc;
		} else {
			return None;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		if (keyword != null) sql.append(keyword);
	}
}
