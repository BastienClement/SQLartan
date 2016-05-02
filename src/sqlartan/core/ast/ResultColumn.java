package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.IdentifierToken;
import static sqlartan.core.ast.token.KeywordToken.AS;
import static sqlartan.core.ast.token.OperatorToken.DOT;
import static sqlartan.core.ast.token.OperatorToken.MUL;

public interface ResultColumn extends Node {
	static ResultColumn parse(ParserContext context) {
		return context.alternatives(
			() -> {
				context.consume(MUL);
				return Wildcard.singleton;
			},
			() -> {
				String table = context.consume(IdentifierToken.class).value;
				context.consume(DOT);
				context.consume(MUL);
				return new TableWildcard(table);
			},
			() -> {
				Expr expr = new Expr(context.parse(Expression::parse));
				if (context.tryConsume(AS)) {
					expr.alias = context.consumeIdentifier().value;
				}
				return expr;
			}
		);
	}

	class Expr implements ResultColumn {
		public Expression expr;
		public String alias;

		public Expr(sqlartan.core.ast.Expression expr) {
			this.expr = expr;
		}

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.append(expr);
			if (alias != null)
				sql.append(" AS ").append(alias);
		}
	}

	class Wildcard implements ResultColumn {
		public static final Wildcard singleton = new Wildcard();
		private Wildcard() {}

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.append("*");
		}
	}

	class TableWildcard implements ResultColumn {
		public String table;

		public TableWildcard(String table) {
			this.table = table;
		}

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.appendIdentifier(table).append(".*");
		}
	}
}
