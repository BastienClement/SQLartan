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
	public Optional<String> schema;
	public String name;

	public CreateTableStatement() {}
	public CreateTableStatement(boolean t, boolean ine, Optional<String> s, String n) {
		this.temporary = t;
		this.ifNotExists = ine;
		this.schema = s;
		this.name = n;
	}

	public static CreateTableStatement parse(ParserContext context) {
		boolean temporary = context.tryConsume(TEMP) || context.tryConsume(TEMPORARY);
		context.consume(TABLE);
		boolean ifNotExists = context.tryConsume(IF, NOT, EXISTS);
		Optional<String> schema = context.optConsumeSchema();
		String name = context.consumeIdentifier();

		if (context.current(AS)) {
			return As.parse(context, temporary, ifNotExists, schema, name);
		} else {
			return Def.parse(context, temporary, ifNotExists, schema, name);
		}
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(CREATE);
		if (temporary) sql.append(TEMPORARY);
		sql.append(TABLE);
		if (ifNotExists) sql.append(IF, NOT, EXISTS);
		schema.ifPresent(sql::appendSchema);
		sql.appendIdentifier(name);
	}

	/**
	 * CREATE TABLE ... ( columns ... )
	 */
	public static class Def extends CreateTableStatement {
		public List<ColumnDefinition> columns;
		public List<TableConstraint> contraints = new ArrayList<>();
		public boolean withoutRowid;

		public Def() {}
		private Def(boolean t, boolean ine, Optional<String> s, String n) {
			super(t, ine, s, n);
		}

		public static Def parse(ParserContext context, boolean t, boolean ine, Optional<String> s, String n) {
			Def create = new Def(t, ine, s, n);
			context.consume(LEFT_PAREN);
			create.columns = context.parseList(ColumnDefinition::parse);
			context.parseList(create.contraints, TableConstraint::parse);
			context.consume(RIGHT_PAREN);
			create.withoutRowid = context.tryConsume(WITHOUT, ROWID);
			return create;
		}
	}

	/**
	 * CREATE TABLE ... AS ...
	 */
	public static class As extends CreateTableStatement {
		public SelectStatement select;

		public As() {}
		private As(boolean t, boolean ine, Optional<String> s, String n) {
			super(t, ine, s, n);
		}

		public static As parse(ParserContext context, boolean t, boolean ine, Optional<String> s, String n) {
			As create = new As(t, ine, s, n);
			create.select = SelectStatement.parse(context);
			return create;
		}
	}
}
