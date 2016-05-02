package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.KeywordToken.*;
import static sqlartan.core.ast.token.OperatorToken.DOT;

public abstract class AlterTableStatement implements Statement {
	public String schema;
	public String table;

	public static AlterTableStatement parse(ParserContext context) {
		AlterTableStatement alter;

		context.consume(ALTER);
		context.consume(TABLE);

		String schema = null;
		if (context.next(DOT)) {
			schema = context.consumeIdentifier().value;
			context.consume(DOT);
		}

		String table = context.consumeIdentifier().value;

		if (context.tryConsume(RENAME)) {
			context.consume(TO);

			RenameTo rename = new RenameTo();
			rename.newName = context.consumeIdentifier().value;
			alter = rename;
		} else {
			context.consume(ADD);
			context.tryConsume(COLUMN);

			AddColumn addColumn = new AddColumn();
			addColumn.columnDefinition = context.parse(ColumnDefinition::parse);
			alter = addColumn;
		}

		if (schema != null) alter.schema = schema;
		alter.table = table;
		return alter;
	}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append("ALTER TABLE ");
		if (schema != null)
			sql.append(schema).append(".");
		sql.append(table);
	}

	public static class RenameTo extends AlterTableStatement {
		public String newName;

		@Override
		public void toSQL(SQLBuilder sql) {
			super.toSQL(sql);
			sql.append(" RENAME TO ").append(newName);
		}
	}

	public static class AddColumn extends AlterTableStatement {
		public ColumnDefinition columnDefinition;

		@Override
		public void toSQL(SQLBuilder sql) {
			super.toSQL(sql);
			sql.append(" ADD COLUMN ").append(columnDefinition);
		}
	}
}
