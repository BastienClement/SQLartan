package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.SEMICOLON;
import static sqlartan.util.Matching.match;

/**
 * https://www.sqlite.org/lang_createtrigger.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class CreateTriggerStatement extends CreateStatement {
	public enum Timing {Undefined, Before, After, InsteadOf}

	public enum Trigger {Delete, Insert, Update}

	public boolean temporary;
	public boolean ifNotExists;
	public Optional<String> schema = Optional.empty();
	public String name;
	public Timing timing = Timing.Undefined;
	public Trigger trigger;
	public List<String> columns = new ArrayList<>();
	public String table;
	public boolean forEachRow;
	public Optional<Expression> when = Optional.empty();
	public List<Statement> body;

	public static CreateTriggerStatement parse(ParserContext context) {
		CreateTriggerStatement trigger = new CreateTriggerStatement();

		trigger.temporary = context.tryConsume(TEMP) || context.tryConsume(TEMPORARY);
		context.consume(TRIGGER);
		trigger.ifNotExists = context.tryConsume(IF, NOT, EXISTS);
		trigger.schema = context.optConsumeSchema();
		trigger.name = context.consumeIdentifier();

		trigger.timing = match(context.current())
			.when(BEFORE, () -> {
				context.consume(BEFORE);
				return Timing.Before;
			})
			.when(AFTER, () -> {
				context.consume(AFTER);
				return Timing.After;
			})
			.when(INSTEAD, () -> {
				context.consume(INSTEAD, OF);
				return Timing.InsteadOf;
			})
			.orElse(Timing.Undefined);

		trigger.trigger = match(context.current())
			.when(DELETE, () -> {
				context.consume(DELETE);
				return Trigger.Delete;
			})
			.when(INSERT, () -> {
				context.consume(INSERT);
				return Trigger.Insert;
			})
			.when(UPDATE, () -> {
				context.consume(UPDATE);
				if (context.tryConsume(OF)) {
					trigger.columns = context.parseList(ParserContext::consumeIdentifier);
				}
				return Trigger.Update;
			})
			.orElseThrow(ParseException.UnexpectedCurrentToken);

		context.consume(ON);
		trigger.table = context.consumeIdentifier();
		trigger.forEachRow = context.tryConsume(FOR, EACH, ROW);

		if (context.tryConsume(WHEN)) {
			trigger.when = Optional.of(Expression.parse(context));
		}

		context.consume(BEGIN);
		trigger.body = context.parseList(SEMICOLON, ctx ->
			match(ctx.current(), Statement.class)
				.when(UPDATE, () -> UpdateStatement.parse(ctx))
				.when(INSERT, () -> InsertStatement.parse(ctx))
				.when(DELETE, () -> DeleteStatement.parse(ctx))
				.when(SELECT, () -> SelectStatement.parse(ctx))
				.when(END, () -> {
					throw ParseException.UnexpectedCurrentToken;
				})
				.orElse(() -> ctx.alternatives(
					UpdateStatement::parse,
					InsertStatement::parse,
					DeleteStatement::parse,
					SelectStatement::parse
				))
		);
		context.consume(SEMICOLON, END);

		return trigger;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(CREATE);
		if (temporary) sql.append(TEMPORARY);
		sql.append(TRIGGER);
		if (ifNotExists) sql.append(IF, NOT, EXISTS);
		sql.appendSchema(schema).appendIdentifier(name);
		switch (timing) {
			case Before:
				sql.append(BEFORE);
				break;
			case After:
				sql.append(AFTER);
				break;
			case InsteadOf:
				sql.append(INSTEAD, OF);
				break;
		}
		switch (trigger) {
			case Delete:
				sql.append(DELETE);
				break;
			case Insert:
				sql.append(INSERT);
				break;
			case Update:
				sql.append(UPDATE);
				if (!columns.isEmpty()) sql.append(OF).appendIdentifiers(columns);
				break;
		}
		sql.append(ON).appendIdentifier(table);
		if (forEachRow) sql.append(FOR, EACH, ROW);
		when.ifPresent(w -> sql.append(WHEN).append(w));
		sql.append(BEGIN).append(body, SEMICOLON).append(SEMICOLON, END);
	}
}
