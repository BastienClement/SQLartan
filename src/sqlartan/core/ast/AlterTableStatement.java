package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.core.ast.token.Operator.DOT;

/**
 * https://www.sqlite.org/lang_altertable.html
 */
public abstract class AlterTableStatement implements Statement {
	public String schema;
	public String table;

	public static AlterTableStatement parse(ParserContext context) {
		AlterTableStatement alter;

		context.consume(ALTER);
		context.consume(TABLE);

		String schema = null;
		if (context.next(DOT)) {
			schema = context.consumeIdentifier();
			context.consume(DOT);
		}

		String table = context.consumeIdentifier();

		if (context.tryConsume(RENAME)) {
			context.consume(TO);
			alter = new RenameTo(context.consumeIdentifier());
		} else {
			context.consume(ADD);
			context.tryConsume(COLUMN);
			alter = new AddColumn(ColumnDefinition.parse(context));
		}

		if (schema != null) alter.schema = schema;
		alter.table = table;

		return alter;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append("ALTER TABLE ");
		if (schema != null)
			sql.append(schema).append(".");
		sql.append(table);
	}

	public static class RenameTo extends AlterTableStatement {
		public String name;

		public RenameTo() {}
		public RenameTo(String name) {
			this.name = name;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(" RENAME TO ").append(name);
		}
	}

	public static class AddColumn extends AlterTableStatement {
		public ColumnDefinition column;

		public AddColumn() {}
		public AddColumn(ColumnDefinition column) {
			this.column = column;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(" ADD COLUMN ").append(column);
		}
	}
}
