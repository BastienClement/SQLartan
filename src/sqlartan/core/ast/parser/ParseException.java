package sqlartan.core.ast.parser;

public class ParseException extends Exception {
	public final String source;
	public final int offset;

	public ParseException(String message, String source, int offset) {
		super(message);
		this.source = source;
		this.offset = offset;
	}
}
