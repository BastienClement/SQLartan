package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.parser.Util;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.PRAGMA;
import static sqlartan.core.ast.Operator.*;

/**
 * https://www.sqlite.org/pragma.html#syntax
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class PragmaStatement implements Statement {
	public Optional<String> schema;
	public String pragma;

	public static PragmaStatement parse(ParserContext context) {
		context.consume(PRAGMA);

		Optional<String> schema = context.optConsumeSchema();

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
	public PragmaStatement(Optional<String> schema, String pragma) {
		this.schema = schema;
		this.pragma = pragma;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(PRAGMA);
		schema.ifPresent(sql::appendSchema);
		sql.appendRaw(pragma);
	}

	public static class Get extends PragmaStatement {
		public Get() {}
		public Get(Optional<String> schema, String pragma) {
			super(schema, pragma);
		}
	}

	public static class Set extends PragmaStatement {
		public String value;

		public Set() {}
		public Set(Optional<String> schema, String pragma, String value) {
			super(schema, pragma);
			this.value = value;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(EQ).appendTextLiteral(value);
		}
	}

	public static class Call extends PragmaStatement {
		public String value;

		public Call() {}
		public Call(Optional<String> schema, String pragma, String value) {
			super(schema, pragma);
			this.value = value;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(LEFT_PAREN)
			   .appendTextLiteral(value)
			   .append(RIGHT_PAREN);
		}
	}
}
