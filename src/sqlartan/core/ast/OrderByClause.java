package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.List;
import static sqlartan.core.ast.Keyword.BY;
import static sqlartan.core.ast.Keyword.ORDER;

@SuppressWarnings("WeakerAccess")
public class OrderByClause implements Node {
	public List<OrderingTerm> terms;

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static OrderByClause parse(ParserContext context) {
		OrderByClause orderBy = new OrderByClause();
		context.consume(ORDER, BY);
		orderBy.terms = context.parseList(OrderingTerm::parse);
		return orderBy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(ORDER, BY).append(terms);
	}
}
