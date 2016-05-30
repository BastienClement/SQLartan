package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.ANALYZE;
import static sqlartan.core.ast.Operator.DOT;

/**
 * https://www.sqlite.org/lang_analyze.html
 */
@SuppressWarnings("WeakerAccess")
public class AnalyzeStatement implements Statement {
	public String schema;
	public String subject;
	public boolean ambiguous;

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static Statement parse(ParserContext context) {
		context.consume(ANALYZE);
		AnalyzeStatement analyze = new AnalyzeStatement();
		if (context.next(DOT)) {
			analyze.schema = context.consumeIdentifier();
			context.consume(DOT);
			analyze.subject = context.consumeIdentifier();
			analyze.ambiguous = false;
		} else {
			analyze.schema = analyze.subject = context.consumeIdentifier();
			analyze.ambiguous = true;
		}
		return analyze;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(ANALYZE);
		if (ambiguous || schema == null) {
			sql.appendIdentifier(subject);
		} else {
			sql.appendIdentifier(schema)
			   .append(DOT)
			   .appendIdentifier(subject);
		}
	}
}
