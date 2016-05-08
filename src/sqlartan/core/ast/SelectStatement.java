package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
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

		Compound.Operator op;
		while ((op = context.optParse(Compound.Operator::parse).orElse(null)) != null) {
			Core rhs = Core.parse(context);
			Compound compound = new Compound();
			compound.lhs = lhs;
			compound.operator = op;
			compound.rhs = rhs;
			lhs = compound;
		}

		if (lhs instanceof SelectCoreStatement) {
			SelectCoreStatement core = (SelectCoreStatement) lhs;
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
		} else if (lhs instanceof Compound) {
			Compound compound = (Compound) lhs;

			if (compound.rhs instanceof SelectCoreStatement) {
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
	abstract class Core implements SelectStatement, Compoundable {
		public static Core parse(ParserContext context) {
			if (context.current(VALUES)) {
				return ValuesStatement.parse(context);
			} else {
				return SelectCoreStatement.parse(context);
			}
		}
	}

	/**
	 * A simple select statement
	 */
	class Simple implements SelectStatement {
		public boolean distinct;
		public List<ResultColumn> columns = new ArrayList<>();
		public Optional<SelectSource> from = Optional.empty();
		public Optional<WhereClause> where = Optional.empty();
		public List<Expression> groupBy = new ArrayList<>();
		public Optional<Expression> having = Optional.empty();

		public Optional<OrderByClause> orderBy = Optional.empty();
		public Optional<LimitClause> limit = Optional.empty();
	}

	/**
	 * A compound select statement
	 */
	class Compound implements SelectStatement, Compoundable {
		public enum Operator implements Node.Enumerated {
			Union(UNION),
			UnionAll(UNION, ALL),
			Intersect(INTERSECT),
			Except(EXCEPT);

			private Keyword[] keywords;

			Operator(Keyword... keywords) {
				this.keywords  = keywords;
			}

			public static Operator parse(ParserContext context) {
				switch (context.consume(Token.Keyword.class).node()) {
					case UNION:
						if (context.tryConsume(ALL)) {
							return UnionAll;
						} else {
							return Union;
						}
					case INTERSECT:
						return Intersect;
					case EXCEPT:
						return Except;
					default:
						throw ParseException.UnexpectedCurrentToken(UNION, INTERSECT, EXCEPT);
				}
			}

			@Override
			public void toSQL(Builder sql) {
				sql.append(keywords);
			}
		}

		public Compoundable lhs;
		public Operator operator;
		public Compoundable rhs;

		public Optional<OrderByClause> orderBy = Optional.empty();
		public Optional<LimitClause> limit = Optional.empty();
	}
}
