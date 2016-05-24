package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_select.html
 * "OMG SELECT STATEMENTS ARE SO PAINFUL TO PARSE!"
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public interface SelectStatement extends Statement {
	/**
	 * General SELECT statement parser
	 * Currently delegates to Simple parser.
	 * TODO: handle compound selects here
	 */
	static SelectStatement parse(ParserContext context) {
		Compoundable lhs = Core.parse(context);

		CompoundSelectStatement.Operator op;
		while ((op = context.optParse(CompoundSelectStatement.Operator::parse).orElse(null)) != null) {
			Core rhs = Core.parse(context);
			CompoundSelectStatement compound = new CompoundSelectStatement();
			compound.lhs = lhs;
			compound.operator = op;
			compound.rhs = rhs;
			lhs = compound;
		}

		if (lhs instanceof CoreSelectStatement) {
			CoreSelectStatement core = (CoreSelectStatement) lhs;
			Simple simple = new Simple();

			simple.distinct = core.distinct;
			simple.columns = core.columns;
			simple.from = core.from;
			simple.where = core.where;
			simple.groupBy = core.groupBy;
			simple.having = core.having;

			// ORDER BY
			if (context.current(ORDER)) {
				simple.orderBy = Optional.of(OrderByClause.parse(context));
			}

			// LIMIT .. OFFSET
			if (context.current(LIMIT)) {
				simple.limit = Optional.of(LimitClause.parse(context));
			}

			return simple;
		} else if (lhs instanceof CompoundSelectStatement) {
			CompoundSelectStatement compound = (CompoundSelectStatement) lhs;

			if (compound.rhs instanceof CoreSelectStatement) {
				// ORDER BY
				if (context.current(ORDER)) {
					compound.orderBy = Optional.of(OrderByClause.parse(context));
				}

				// LIMIT .. OFFSET
				if (context.current(LIMIT)) {
					compound.limit = Optional.of(LimitClause.parse(context));
				}
			}
		}

		return lhs;
	}

	/**
	 * Common super-type for compoundable statements
	 * Used by Core and Compound, but not Simple
	 */
	interface Compoundable extends SelectStatement {}

	/**
	 * The core a SELECT statement
	 */
	interface Core extends SelectStatement, Compoundable {
		static Core parse(ParserContext context) {
			System.out.println("stop");
			if (context.current(VALUES)) {
				System.out.println("sas");
				return ValuesStatement.parse(context);
			} else {
				return CoreSelectStatement.parse(context);
			}
		}
	}

	abstract class CoreProperties implements Node {
		public boolean distinct;
		public List<ResultColumn> columns = new ArrayList<>();
		public Optional<SelectSource> from = Optional.empty();
		public Optional<WhereClause> where = Optional.empty();
		public List<Expression> groupBy = new ArrayList<>();
		public Optional<Expression> having = Optional.empty();

		@Override
		public void toSQL(Builder sql) {
			sql.append(SELECT);
			if (distinct) sql.append(DISTINCT);
			sql.append(columns);
			from.ifPresent(f -> sql.append(FROM).append(f));
			sql.append(where);
			if (!groupBy.isEmpty()) {
				sql.append(GROUP, BY).append(groupBy);
				having.ifPresent(h -> sql.append(HAVING).append(h));
			}
		}
	}

	/**
	 * A simple select statement
	 */
	class Simple extends CoreProperties implements SelectStatement {
		public Optional<OrderByClause> orderBy = Optional.empty();
		public Optional<LimitClause> limit = Optional.empty();

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(orderBy, limit);
		}
	}

}
