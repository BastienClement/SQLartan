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

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
		public LiteralValue value;

		public static Set parse(ParserContext context) {
			context.consume(EQ);
			Set set = new Set();
			set.value = LiteralValue.parseValue(context);
			return set;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(EQ).append(value);
		}
	}

	/**
	 * PRAGMA ... ( ... ) ;
	 */
	public static class Call extends PragmaStatement {
		public LiteralValue value;

		/**
		 * @see sqlartan.core.ast.parser.Parser
		 */
		public static Call parse(ParserContext context) {
			context.consume(LEFT_PAREN);
			Call call = new Call();
			call.value = LiteralValue.parseValue(context);
			context.consume(RIGHT_PAREN);
			return call;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(LEFT_PAREN)
			   .append(value)
			   .append(RIGHT_PAREN);
		}
	}
}
