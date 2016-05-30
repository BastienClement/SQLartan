package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import sqlartan.util.Matching;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.util.Matching.match;

/**
 * CREATE is a prefix for multiple statements
 * This class perform the step of disambiguation and delegates further parsing to concrete implementation
 */
public abstract class CreateStatement implements Statement {
	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static CreateStatement parse(ParserContext context) {
		context.begin();
		context.consume(CREATE);
		Token current = context.current();
		Token next = context.next();
		context.rollback();;

		return doMatch(current, context).orElse(
			() -> doMatch(next, context).orElseThrow(ParseException.UnexpectedCurrentToken(INDEX, TABLE, TRIGGER, VIEW, VIRTUAL))
		);
	}

	private static Matching<Token>.Returning<CreateStatement> doMatch(Token token, ParserContext context) {
		return match(token, CreateStatement.class)
			.when(INDEX, () -> CreateIndexStatement.parse(context))
			.when(TABLE, () -> CreateTableStatement.parse(context))
			.when(TRIGGER, () -> CreateTriggerStatement.parse(context))
			.when(VIEW, () -> CreateViewStatement.parse(context))
			.when(VIRTUAL, () -> CreateVirtualTableStatement.parse(context));
	}
}
