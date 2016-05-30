package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_delete.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class DeleteStatement implements Statement {
	public QualifiedTableName table;
	public Optional<WhereClause> where = Optional.empty();
	public Optional<OrderByClause> orderBy = Optional.empty();
	public Optional<LimitClause> limit = Optional.empty();

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static DeleteStatement parse(ParserContext context) {
		DeleteStatement delete = new DeleteStatement();
		context.consume(DELETE, FROM);
		delete.table = QualifiedTableName.parse(context);
		if (context.current(WHERE)) {
			delete.where = Optional.of(WhereClause.parse(context));
		}
		if (context.current(ORDER)) {
			delete.orderBy = Optional.of(OrderByClause.parse(context));
		}
		if (delete.orderBy.isPresent() || context.current(LIMIT)) {
			delete.limit = Optional.of(LimitClause.parse(context));
		}
		return delete;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(DELETE, FROM).append(table)
		   .append(where, orderBy, limit);
	}
}
