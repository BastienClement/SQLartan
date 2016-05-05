package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.util.Matching.match;

/**
 * https://www.sqlite.org/lang_transaction.html
 */
public class BeginStatement implements Statement {
	public enum Mode {
		None,
		Deferred,
		Immediate,
		Exclusive
	}

	public Mode mode = Mode.None;

	public static BeginStatement parse(ParserContext context) {
		context.consume(BEGIN);

		BeginStatement begin = new BeginStatement();
		begin.mode = match(context.current())
			.when(DEFERRED, () -> Mode.Deferred)
			.when(IMMEDIATE, () -> Mode.Immediate)
			.when(EXCLUSIVE, () -> Mode.Exclusive)
			.orElse(Mode.None);

		if (begin.mode != Mode.None) {
			context.consume();
		}

		context.tryConsume(TRANSACTION);
		return begin;
	}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append("BEGIN")
		   .append(match(mode)
			   .when(Mode.Deferred, () -> " DEFERRED")
			   .when(Mode.Immediate, () -> " IMMEDIATE")
			   .when(Mode.Exclusive, () -> " EXCLUSIVE")
			   .orElse(""));
	}
}
