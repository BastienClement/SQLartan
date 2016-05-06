package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_delete.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class DeleteStatement implements Statement {
	public Optional<WithClause> with = Optional.empty();
	public QualifiedTableName table;
	public Optional<Expression> where;
	public List<OrderingTerm> orderBy = new ArrayList<>();
	public Optional<LimitClause> limit;

	public static DeleteStatement parse(ParserContext context) {
		DeleteStatement delete = new DeleteStatement();
		context.consume(DELETE, FROM);
		delete.table = QualifiedTableName.parse(context);
		if (context.tryConsume(WHERE)) {
			delete.where = Optional.of(Expression.parse(context));
		}
		if (context.tryConsume(ORDER, BY)) {
			delete.orderBy = context.parseList(OrderingTerm::parse);
		}
		if (context.current(LIMIT)) {
			delete.limit = Optional.of(LimitClause.parse(context));
		}
		return delete;
	}

	@Override
	public void toSQL(Builder sql) {
		with.ifPresent(sql::append);
		sql.append(DELETE, FROM).append(table);
		where.ifPresent(w -> sql.append(WHERE).append(w));
		if (!orderBy.isEmpty()) {
			sql.append(ORDER, BY).append(orderBy);
		}
		limit.ifPresent(l -> sql.append(LIMIT).append(l));
	}
}
