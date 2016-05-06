package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import static sqlartan.core.ast.Operator.MINUS;
import static sqlartan.core.ast.Operator.PLUS;

/**
 * https://www.sqlite.org/syntax/signed-number.html
 */
@SuppressWarnings("WeakerAccess")
public class SignedNumber implements Node {
	public enum Sign {
		None, Plus, Minus;
	}

	public Sign sign = Sign.None;
	public String value;

	public static SignedNumber parse(ParserContext context) {
		SignedNumber number = new SignedNumber();
		if (context.tryConsume(PLUS)) {
			number.sign = Sign.Plus;
		} else if (context.tryConsume(MINUS)) {
			number.sign = Sign.Minus;
		}
		number.value = context.consume(Token.Literal.Numeric.class).value;
		return number;
	}

	@Override
	public void toSQL(Builder sql) {
		if (sign == Sign.Minus) sql.appendUnary(MINUS);
		sql.appendRaw(value);
	}
}
