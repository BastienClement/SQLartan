package sqlartan.core.ast.parser;

import java.util.function.Supplier;

/**
 * https://blogs.oracle.com/jrose/entry/longjumps_considered_inexpensive
 */
public abstract class FastParseException extends RuntimeException implements Supplier<FastParseException> {
	public abstract ParseException materialize(ParserContext context);

	@Override
	public Throwable fillInStackTrace() { return null; }

	@Override
	public FastParseException get() { return this; }
}
