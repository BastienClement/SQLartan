package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.*;

/**
 * https://www.sqlite.org/lang_createtable.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class CreateTableStatement extends CreateStatement {
	public boolean temporary;
	public boolean ifNotExists;
	public Optional<String> schema = Optional.empty();
	public String name;

	public static CreateTableStatement parse(ParserContext context) {
		boolean temporary = context.tryConsume(TEMP) || context.tryConsume(TEMPORARY);

		context.consume(TABLE);

		boolean ifNotExists = context.tryConsume(IF, NOT, EXISTS);
		Optional<String> schema = context.optConsumeSchema();
		String name = context.consumeIdentifier();

		CreateTableStatement create;

		if (context.current(AS)) {
			create = As.parse(context);
		} else {
			create = Def.parse(context);
		}

		create.temporary = temporary;
		create.ifNotExists = ifNotExists;
		create.schema = schema;
		create.name = name;

		return create;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(CREATE);
		if (temporary) sql.append(TEMPORARY);
		sql.append(TABLE);
		if (ifNotExists) sql.append(IF, NOT, EXISTS);
		sql.appendSchema(schema).appendIdentifier(name);
	}

	/**
	 * CREATE TABLE ... ( columns ... ) ;
	 */
	public static class Def extends CreateTableStatement {
		public List<ColumnDefinition> columns;
		public List<TableConstraint> constraints = new ArrayList<>();
		public boolean withoutRowid;

		public static Def parse(ParserContext context) {
			Def create = new Def();
			context.consume(LEFT_PAREN);
			create.columns = context.parseList(ColumnDefinition::parse);
			context.parseList(create.constraints, TableConstraint::parse);
			context.consume(RIGHT_PAREN);
			create.withoutRowid = context.tryConsume(WITHOUT, ROWID);
			return create;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(LEFT_PAREN).append(columns);
			if (!constraints.isEmpty()) sql.append(COMMA).append(constraints);
			sql.append(RIGHT_PAREN);
			if (withoutRowid) sql.append(WITHOUT, ROWID);
		}
	}

	/**
	 * CREATE TABLE ... AS ... ;
	 */
	public static class As extends CreateTableStatement {
		public SelectStatement select;

		public static As parse(ParserContext context, boolean t, boolean ine, Optional<String> s, String n) {
			As create = new As();
			create.select = SelectStatement.parse(context);
			return create;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(AS).append(select);
		}
	}
}
