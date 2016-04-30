package sqlartan.core.ast.token;

import sqlartan.core.ast.parser.ParseException;

public class TokenizeException extends ParseException {
	public TokenizeException(String message, String source, int offset) {
		super(message, source, offset);
	}
}
