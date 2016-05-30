package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.COMMA;

/**
 * https://www.sqlite.org/syntax/join-clause.html
 */
@SuppressWarnings({ "WeakerAccess", "OptionalUsedAsFieldOrParameterType" })
public class JoinClause extends SelectSource {
	public enum Join implements Node.Enumerated {
		Left(LEFT),
		Inner(INNER),
		Cross(CROSS);

		private KeywordOrOperator keyword;

		Join(KeywordOrOperator keyword) {
			this.keyword = keyword;
		}

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Join parse(ParserContext context) {
			if (context.tryConsume(COMMA)) {
				return Inner;
			} else {
				Join join;
				switch (context.consume(Token.Keyword.class).node()) {
					case LEFT:
						context.tryConsume(OUTER);
						join = Left;
						break;
					case INNER:
						join = Inner;
						break;
					case CROSS:
						join = Cross;
						break;
					case JOIN:
						return Inner;
					default:
						throw ParseException.UnexpectedCurrentToken(LEFT, INNER, CROSS, COMMA);
				}
				context.consume(JOIN);
				return join;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			sql.append(keyword).append(JOIN);
		}
	}

	public boolean natural;
	public SelectSource lhs;
	public Join join;
	public SelectSource rhs;
	public Optional<JoinConstraint> constraint = Optional.empty();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(lhs);
		if (natural) sql.append(NATURAL);
		sql.append(join).append(rhs).append(constraint);
	}
}
