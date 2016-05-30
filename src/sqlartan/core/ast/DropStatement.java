package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.util.Matching.match;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class DropStatement implements Statement {
	public boolean ifExists;
	public Optional<String> schema = Optional.empty();
	protected Keyword type;

	/**
	 * @see sqlartan.core.ast.parser.Parser
	 */
	public static DropStatement parse(ParserContext context) {
		return match(context.next(), DropStatement.class)
			.when(INDEX, () -> DropIndexStatement.parse(context))
			.when(TABLE, () -> DropTableStatement.parse(context))
			.when(TRIGGER, () -> DropTriggerStatement.parse(context))
			.when(VIEW, () -> DropViewStatement.parse(context))
			.orElseThrow(ParseException.UnexpectedCurrentToken(INDEX, TABLE, TRIGGER, VIEW));
	}

	protected static <T extends DropStatement> T parseDrop(ParserContext context, Keyword type, Supplier<T> create, Consumer<T> decorator) {
		T drop = create.get();
		drop.type = type;
		context.consume(DROP, type);
		drop.ifExists = context.tryConsume(IF, EXISTS);
		drop.schema = context.optConsumeSchema();
		decorator.accept(drop);
		return drop;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(DROP, type);
		if (ifExists) sql.append(IF, EXISTS);
		sql.appendSchema(schema);
	}
}
