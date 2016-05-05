package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
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
			schema = context.consumeIdentifier().value;
			context.consume(DOT);
		}

		String name = context.consumeIdentifier().value;
		PragmaStatement pragma;

		if (context.tryConsume(EQ)) {
			pragma = new Set(schema, name, context.parse(PragmaStatement::parsePragmaValue));
		} else if (context.tryConsume(LEFT_PAREN)) {
			pragma = new Call(schema, name, context.parse(PragmaStatement::parsePragmaValue));
			context.consume(RIGHT_PAREN);
		} else {
			pragma = new Get(schema, name);
		}

		return pragma;
	}

	public static String parsePragmaValue(ParserContext context) {
		return context.alternatives(
			() -> context.parse(Util::parseSignedNumber),
			() -> context.consumeTextLiteral().value,
			() -> context.consumeIdentifier().value
		);
	}

	public PragmaStatement() {}
	public PragmaStatement(String schema, String pragma) {
		this.schema = schema;
		this.pragma = pragma;
	}

	@Override
	public void toSQL(SQLBuilder sql) {
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
		public void toSQL(SQLBuilder sql) {
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
		public void toSQL(SQLBuilder sql) {
			super.toSQL(sql);
			sql.append("(").appendTextLiteral(value).append(")");
		}
	}
}
