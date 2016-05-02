package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Literal;
import static sqlartan.core.ast.token.Keyword.RAISE;
import static sqlartan.util.Matching.match;

public interface Expression extends Node {
	static Expression parse(ParserContext context) {
		Parser<Expression> parser = match(context.current()).<Parser<Expression>>returning()
			.when(Literal.class, tok -> LiteralExpression::parse)
			.when(RAISE, () -> RaiseFunction::parse)
			.orElseThrow(ParseException.UnexpectedCurrentToken);
		return context.parse(parser);
	}

	abstract class LiteralExpression implements Expression {
		public String value;
		static LiteralExpression parse(ParserContext context) {
			LiteralExpression expr = match(context.current(), LiteralExpression.class)
				.when(Literal.Text.class, text -> new TextLiteral())
				.when(Literal.Numeric.class, num -> new NumericLiteral())
				.orElseThrow(ParseException.UnexpectedCurrentToken);
			expr.value = context.consume(Literal.class).value;
			return expr;
		}
	}

	class TextLiteral extends LiteralExpression {
		@Override
		public void toSQL(SQLBuilder sql) {
			sql.appendTextLiteral(value);
		}
	}

	class NumericLiteral extends LiteralExpression {
		@Override
		public void toSQL(SQLBuilder sql) {
			sql.append(value);
		}
	}

	abstract class RaiseFunction implements Expression {
		static RaiseFunction parse(ParserContext context) {
			return null;
		}
	}
}
