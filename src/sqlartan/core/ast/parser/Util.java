package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.Literal;
import java.util.function.Supplier;
import static sqlartan.core.ast.token.Operator.MINUS;
import static sqlartan.core.ast.token.Operator.PLUS;

public abstract class Util {
	public static String parseSignedNumber(ParserContext context) {
		String sign = "";
		if (context.tryConsume(PLUS)) {
			//sign = "+";
		} else if (context.tryConsume(MINUS)) {
			sign = "-";
		}
		return sign + context.consume(Literal.Numeric.class).value;
	}

	public static Supplier<String> consumeSignedNumber(ParserContext context) {
		return () -> parseSignedNumber(context);
	}
}
