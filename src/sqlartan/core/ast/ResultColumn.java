package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.AS;
import static sqlartan.core.ast.Operator.DOT;
import static sqlartan.core.ast.Operator.MUL;

/**
 * https://www.sqlite.org/syntaxdiagrams.html#result-column
 */
@SuppressWarnings({ "WeakerAccess", "OptionalUsedAsFieldOrParameterType" })
public abstract class ResultColumn implements Node {
	public static ResultColumn parse(ParserContext context) {
		return context.alternatives(
			Wildcard::parse,
			TableWildcard::parse,
			Expr::parse
		);
	}

	/**
	 * SELECT a , 2, sqlite_version() ... ;
	 */
	public static class Expr extends ResultColumn {
		public Expression expression;
		public Optional<String> alias = Optional.empty();

		public static Expr parse(ParserContext context) {
			Expr expr = new Expr();
			expr.expression = Expression.parse(context);
			if (context.tryConsume(AS)) {
				expr.alias = Optional.of(context.consumeIdentifier());
			}
			return expr;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.append(expression);
			alias.ifPresent(sql.append(AS)::appendIdentifier);
		}
	}

	/**
	 * SELECT * ... ;
	 */
	public static class Wildcard extends ResultColumn {
		public static final Wildcard instance = new Wildcard();

		public static Wildcard parse(ParserContext context) {
			context.consume(MUL);
			return instance;
		}

		private Wildcard() {}

		@Override
		public void toSQL(Builder sql) {
			sql.append(MUL);
		}
	}

	/**
	 * SELECT foo.* ... ;
	 */
	public static class TableWildcard extends ResultColumn {
		public String table;

		public static TableWildcard parse(ParserContext context) {
			TableWildcard wildcard = new TableWildcard();
			wildcard.table = context.consumeIdentifier();
			context.consume(DOT, MUL);
			return wildcard;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.appendIdentifier(table).append(DOT, MUL);
		}
	}
}
