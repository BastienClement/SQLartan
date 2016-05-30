package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.REINDEX;

/**
 * https://www.sqlite.org/lang_reindex.html
 * There is no way for the parser to disambiguate the collation / table / index branches
 * A shared `target` property is used instead
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class ReindexStatement implements Statement {
	public Optional<String> schema = Optional.empty();
	public String target;

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static ReindexStatement parse(ParserContext context) {
		context.consume(REINDEX);
		ReindexStatement reindex = new ReindexStatement();
		reindex.schema = context.optConsumeSchema();
		reindex.target = context.consumeIdentifier();
		return reindex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(REINDEX);
		schema.ifPresent(sql::appendSchema);
		sql.appendIdentifier(target);
	}
}
