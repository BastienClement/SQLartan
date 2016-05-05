package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import static sqlartan.core.ast.Keyword.AS;
import static sqlartan.core.ast.Operator.DOT;
import static sqlartan.core.ast.Operator.MUL;

public abstract class ResultColumn implements Node {
	public static ResultColumn parse(ParserContext context) {
		return context.alternatives(
			() -> {
				context.consume(MUL);
				return Wildcard.singleton;
			},
			() -> {
				String table = context.consume(Token.Identifier.class).value;
				context.consume(DOT);
				context.consume(MUL);
				return new TableWildcard(table);
			},
			() -> {
				Expr expr = new Expr(Expression.parse(context));
				if (context.tryConsume(AS)) {
					expr.alias = context.consumeIdentifier();
				}
				return expr;
			}
		);
	}

	public static class Expr extends ResultColumn {
		public Expression expr;
		public String alias;

		public Expr() {}
		public Expr(Expression expr) {
			this.expr = expr;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.append(expr);
			if (alias != null)
				sql.append(AS).appendIdentifier(alias);
		}
	}

	public static class Wildcard extends ResultColumn {
		public static final Wildcard singleton = new Wildcard();
		private Wildcard() {}

		@Override
		public void toSQL(Builder sql) {
			sql.append(MUL);
		}
	}

	public static class TableWildcard extends ResultColumn {
		public String table;

		public TableWildcard() {}
		public TableWildcard(String table) {
			this.table = table;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.appendIdentifier(table).append(DOT, MUL);
		}
	}
}
