package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.ASC;
import static sqlartan.core.ast.token.Keyword.DESC;

public enum Ordering implements Node {
	None,
	Asc,
	Desc;

	public static Ordering parse(ParserContext context) {
		if (context.tryConsume(ASC)) {
			return Asc;
		} else if (context.tryConsume(DESC)) {
			return Desc;
		} else {
			return None;
		}
	}

	@Override
	public void toSQL(Builder sql) {
		switch (this) {
			case Asc: sql.append("ASC"); break;
			case Desc: sql.append("DESC"); break;
		}
	}
}
