package sqlartan.core.ast;


import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.*;

/**
 * https://www.sqlite.org/lang_createindex.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class CreateIndexStatement extends CreateStatement {
	public boolean unique;
	public boolean ifNotExists;
	public Optional<String> schema = Optional.empty();
	public String name;
	public String table;
	public List<IndexedColumn> columns;
	public Optional<WhereClause> where = Optional.empty();

	public static CreateIndexStatement parse(ParserContext context) {
		CreateIndexStatement create = new CreateIndexStatement();

		if (context.tryConsume(UNIQUE)) {
			create.unique = true;
		}

		context.consume(INDEX);

		if (context.tryConsume(IF)) {
			context.consume(NOT);
			context.consume(EXISTS);
			create.ifNotExists = true;
		}

		if (context.next(DOT)) {
			create.schema = Optional.of(context.consumeIdentifier());
			context.consume(DOT);
		}

		create.name = context.consumeIdentifier();
		context.consume(ON);
		create.table = context.consumeIdentifier();

		context.consume(LEFT_PAREN);
		create.columns = context.parseList(COMMA, IndexedColumn::parse);
		context.consume(RIGHT_PAREN);

		if (context.current(WHERE)) {
			create.where = Optional.of(WhereClause.parse(context));
		}

		return create;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(CREATE);
		if (unique) sql.append(UNIQUE);
		sql.append(INDEX);
		if (ifNotExists) sql.append(IF, NOT, EXISTS);
		sql.appendSchema(schema).appendIdentifier(name)
		   .append(ON).appendIdentifier(table)
		   .append(LEFT_PAREN).append(columns).append(RIGHT_PAREN)
		   .append(where);
	}
}
