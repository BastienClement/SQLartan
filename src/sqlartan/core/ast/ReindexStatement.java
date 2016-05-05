package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.REINDEX;
import static sqlartan.core.ast.token.Operator.DOT;

/**
 * https://www.sqlite.org/lang_reindex.html
 * There is no way for the parser to disambiguate the collation / table / index branches
 * A shared `target` property is used instead
 */
public class ReindexStatement implements Statement {
	public String schema;
	public String target;

	public ReindexStatement() {}

	public ReindexStatement(String schema, String target) {
		this.schema = schema;
		this.target = target;
	}

	public static ReindexStatement parse(ParserContext context) {
		context.consume(REINDEX);
		ReindexStatement reindex = new ReindexStatement();
		if (context.next(DOT)) {
			reindex.schema = context.consumeIdentifier();
			context.consume(DOT);
		}
		reindex.target = context.consumeIdentifier();
		return reindex;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append("REINDEX ");
		if (schema != null) sql.append(schema).append(".");
		sql.append(target);
	}
}
