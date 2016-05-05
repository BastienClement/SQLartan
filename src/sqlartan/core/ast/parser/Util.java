package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Token;
import java.util.function.Supplier;
import static sqlartan.core.ast.Operator.MINUS;
import static sqlartan.core.ast.Operator.PLUS;

public abstract class Util {
	public static String parseSignedNumber(ParserContext context) {
		String sign = "";
		if (context.tryConsume(PLUS)) {
			//sign = "+";
		} else if (context.tryConsume(MINUS)) {
			sign = "-";
		}
		return sign + context.consume(Token.Literal.Numeric.class).value;
	}

	public static Supplier<String> consumeSignedNumber(ParserContext context) {
		return () -> parseSignedNumber(context);
	}
}
