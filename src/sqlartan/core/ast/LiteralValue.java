package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.MINUS;
import static sqlartan.core.ast.Operator.PLUS;
import static sqlartan.util.Matching.match;

/**
 * https://www.sqlite.org/syntaxdiagrams.html#literal-value
 */
@SuppressWarnings("WeakerAccess")
public abstract class LiteralValue extends Expression {
	public static LiteralValue parse(ParserContext context) {
		Optional<LiteralValue> literal = match(context.current(), LiteralValue.class)
			.when(NULL, () -> Null.instance)
			.when(CURRENT_TIME, () -> CurrentTime.instance)
			.when(CURRENT_DATE, () -> CurrentDate.instance)
			.when(CURRENT_TIMESTAMP, () -> CurrentTimestamp.instance)
			.get();

		literal.ifPresent(l -> context.consume());
		return literal.orElseGet(() -> parseValue(context));
	}

	public static LiteralValue parseValue(ParserContext context) {
		return match(context.current(), LiteralValue.class)
			.when(Token.Literal.Numeric.class, t -> Numeric.parse(context))
			.when(Token.Literal.Text.class, t -> Text.parse(context))
			.orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	public static class Numeric extends LiteralValue {
		public enum Sign {
			None, Plus, Minus;
		}

		public Sign sign = Sign.None;
		public String value;

		public static Numeric parse(ParserContext context) {
			Numeric num = new Numeric();
			if (context.tryConsume(PLUS)) {
				num.sign = Sign.Plus;
			} else if (context.tryConsume(MINUS)) {
				num.sign = Sign.Minus;
			}
			num.value = context.consume(Token.Literal.Numeric.class).value;
			return num;
		}

		@Override
		public void toSQL(Builder sql) {
			if (sign == Sign.Minus) sql.appendUnary(MINUS);
			sql.appendRaw(value);
		}
	}

	public static class Text extends LiteralValue {
		public String value;

		public static Text parse(ParserContext context) {
			Text text = new Text();
			text.value = context.consume(Token.Literal.Text.class).value;
			return text;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.appendTextLiteral(value);
		}
	}

	public static class Null extends LiteralValue {
		public static final Null instance = new Null();
		private Null() {}

		@Override
		public void toSQL(Builder sql) {
			sql.append(NULL);
		}
	}

	public static class CurrentTime extends LiteralValue {
		public static final CurrentTime instance = new CurrentTime();
		private CurrentTime() {}

		@Override
		public void toSQL(Builder sql) {
			sql.append(CURRENT_TIME);
		}
	}

	public static class CurrentDate extends LiteralValue {
		public static final CurrentDate instance = new CurrentDate();
		private CurrentDate() {}

		@Override
		public void toSQL(Builder sql) {
			sql.append(CURRENT_DATE);
		}
	}

	public static class CurrentTimestamp extends LiteralValue {
		public static final CurrentTimestamp instance = new CurrentTimestamp();
		private CurrentTimestamp() {}

		@Override
		public void toSQL(Builder sql) {
			sql.append(CURRENT_TIMESTAMP);
		}
	}
}
