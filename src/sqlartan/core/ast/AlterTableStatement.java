package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.core.ast.token.Operator.DOT;

public abstract class AlterTableStatement implements Statement {
	public static AlterTableStatement parse(ParserContext context) {
		context.consume(ALTER);
		context.consume(TABLE);

		String schema = null;
		if (context.next(DOT)) {
			schema = context.consumeIdentifier().value;
			context.consume(DOT);
		}

		String table = context.consumeIdentifier().value;

		AlterTableStatement alter;
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

	public String schema;
	public String table;

	@Override
	public void toSQL(StringBuilder sb) {
		sb.append("ALTER TABLE ");
		if (schema != null) sb.append(schema).append(".");
		sb.append(table);
	}

	public static class RenameTo extends AlterTableStatement {
		public String newName;

		@Override
		public void toSQL(StringBuilder sb) {
			super.toSQL(sb);
			sb.append(" RENAME TO ").append(newName);
		}
	}

	public static class AddColumn extends AlterTableStatement {
		public ColumnDefinition columnDefinition;

		@Override
		public void toSQL(StringBuilder sb) {
			super.toSQL(sb);
			sb.append(" ADD COLUMN ");
			columnDefinition.toSQL(sb);
		}
	}
}
