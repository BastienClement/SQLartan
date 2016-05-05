package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.parser.Util;
import static sqlartan.core.ast.token.Keyword.PRAGMA;
import static sqlartan.core.ast.token.Operator.*;

/**
 * https://www.sqlite.org/pragma.html#syntax
 */
public abstract class PragmaStatement implements Statement {
	public String schema;
	public String pragma;

	public static PragmaStatement parse(ParserContext context) {
		context.consume(PRAGMA);

		String schema = null;
		if (context.next(DOT)) {
			schema = context.consumeIdentifier();
			context.consume(DOT);
		}

		String name = context.consumeIdentifier();
		PragmaStatement pragma;

		if (context.tryConsume(EQ)) {
			pragma = new Set(schema, name, PragmaStatement.parsePragmaValue(context));
		} else if (context.tryConsume(LEFT_PAREN)) {
			pragma = new Call(schema, name, PragmaStatement.parsePragmaValue(context));
			context.consume(RIGHT_PAREN);
		} else {
			pragma = new Get(schema, name);
		}

		return pragma;
	}

	public static String parsePragmaValue(ParserContext context) {
		return context.alternatives(
			Util.consumeSignedNumber(context),
			context::consumeTextLiteral,
			context::consumeIdentifier
		);
	}

	public PragmaStatement() {}
	public PragmaStatement(String schema, String pragma) {
		this.schema = schema;
		this.pragma = pragma;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append("PRAGMA ");
		if (schema != null) sql.append(schema).append(".");
		sql.append(pragma);
	}

	public static class Get extends PragmaStatement {
		public Get() {}
		public Get(String schema, String pragma) {
			super(schema, pragma);
		}
	}

	public static class Set extends PragmaStatement {
		public String value;

		public Set() {}
		public Set(String schema, String pragma, String value) {
			super(schema, pragma);
			this.value = value;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(" = ").appendTextLiteral(value);
		}
	}

	public static class Call extends PragmaStatement {
		public String value;

		public Call() {}
		public Call(String schema, String pragma, String value) {
			super(schema, pragma);
			this.value = value;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append("(").appendTextLiteral(value).append(")");
		}
	}
}
