package sqlartan.core.ast.parser;

/**
 * https://blogs.oracle.com/jrose/entry/longjumps_considered_inexpensive
 */
public abstract class FastParseException extends RuntimeException {
	public abstract ParseException materialize(ParserContext context);

	@Override
	public Throwable fillInStackTrace() { return null; }
}
