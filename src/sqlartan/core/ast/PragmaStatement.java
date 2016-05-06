package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.PRAGMA;
import static sqlartan.core.ast.Operator.*;
import static sqlartan.util.Matching.match;

/**
 * https://www.sqlite.org/pragma.html#syntax
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class PragmaStatement implements Statement {
	public Optional<String> schema = Optional.empty();
	public String pragma;

	public static PragmaStatement parse(ParserContext context) {
		context.consume(PRAGMA);

		Optional<String> schema = context.optConsumeSchema();
		String name = context.consumeIdentifier();

		PragmaStatement pragma = match(context.current(), PragmaStatement.class)
			.when(EQ, context.bind(Set::parse))
			.when(LEFT_PAREN, context.bind(Call::parse))
			.orElse(Get::new);

		pragma.schema = schema;
		pragma.pragma = name;

		return pragma;
	}

	public static String parsePragmaValue(ParserContext context) {
		return context.alternatives(
			() -> SignedNumber.parse(context).toSQL(),
			context::consumeTextLiteral,
			context::consumeIdentifier
		);
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(PRAGMA);
		schema.ifPresent(sql::appendSchema);
		sql.appendRaw(pragma);
	}

	/**
	 * PRAGMA ... ;
	 */
	public static class Get extends PragmaStatement {}

	/**
	 * PRAGMA ... = ... ;
	 */
	public static class Set extends PragmaStatement {
		public String value;

		public static Set parse(ParserContext context) {
			context.consume(EQ);
			Set set = new Set();
			set.value = parsePragmaValue(context);
			return set;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(EQ).appendTextLiteral(value);
		}
	}

	/**
	 * PRAGMA ... ( ... ) ;
	 */
	public static class Call extends PragmaStatement {
		public String value;

		public static Call parse(ParserContext context) {
			context.consume(LEFT_PAREN);
			Call call = new Call();
			call.value = parsePragmaValue(context);
			context.consume(RIGHT_PAREN);
			return call;
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
