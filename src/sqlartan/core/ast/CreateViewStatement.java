package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.LEFT_PAREN;
import static sqlartan.core.ast.Operator.RIGHT_PAREN;

/**
 * https://www.sqlite.org/lang_createview.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class CreateViewStatement extends CreateStatement {
	public boolean temporary;
	public boolean ifNotExists;
	public Optional<String> schema = Optional.empty();
	public String name;
	public List<String> columns = new ArrayList<>();
	public SelectStatement as;

	public static CreateViewStatement parse(ParserContext context) {
		CreateViewStatement create = new CreateViewStatement();
		context.consume(CREATE);
		create.temporary = context.tryConsume(TEMP) || context.tryConsume(TEMPORARY);
		context.consume(VIEW);
		create.ifNotExists = context.tryConsume(IF, NOT, EXISTS);
		create.schema = context.optConsumeSchema();
		create.name = context.consumeIdentifier();
		if (context.tryConsume(LEFT_PAREN)) {
			create.columns = context.parseList(ParserContext::consumeIdentifier);
			context.consume(RIGHT_PAREN);
		}
		context.consume(AS);
		create.as = SelectStatement.parse(context);
		return create;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(CREATE);
		if (temporary) sql.append(TEMPORARY);
		sql.append(VIEW);
		if (ifNotExists) sql.append(IF, NOT, EXISTS);
		sql.appendSchema(schema).appendIdentifier(name);
		if (!columns.isEmpty()) sql.append(LEFT_PAREN).appendIdentifiers(columns).append(RIGHT_PAREN);
		sql.append(AS).append(as);
	}
}
