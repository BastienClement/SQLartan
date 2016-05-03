package sqlartan.core.ast.parser;

import java.util.function.Supplier;

/**
 * An exception without a stacktrace.
 *
 * Instances of this class should be constructed once and throw as many time as required.
 * This allow to skip allocation and initialization costs entirely.
 *
 * https://blogs.oracle.com/jrose/entry/longjumps_considered_inexpensive
 */
public abstract class FastParseException extends RuntimeException implements Supplier<FastParseException> {
	/**
	 * Materialize this exception to a ParseException.
	 * This method will be called when parsing a SQL statement fails and an exception
	 * is thrown to the caller.
	 *
	 * @param context the parser context
	 */
	public abstract ParseException materialize(ParserContext context);

	@Override
	public Throwable fillInStackTrace() { return null; }

	/**
	 * Implements Supplier so it can more easily be used with .orElseThrow method in Matching.
	 */
	@Override
	public FastParseException get() { return this; }
}
