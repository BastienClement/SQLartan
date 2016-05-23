package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * A compound select statement
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CompoundSelectStatement implements SelectStatement, SelectStatement.Compoundable {
	public enum Operator implements Enumerated {
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

	@Override
	public void toSQL(Builder sql) {
		sql.append(lhs).append(operator).append(rhs);
	}
}
