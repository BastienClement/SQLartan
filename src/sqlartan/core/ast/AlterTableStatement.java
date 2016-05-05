package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_altertable.html
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AlterTableStatement implements Statement {
	public Optional<String> schema = Optional.empty();
	public String table;

	public static AlterTableStatement parse(ParserContext context) {
		context.consume(ALTER, TABLE);
		AlterTableStatement alter;

		Optional<String> schema = context.optConsumeSchema();
		String table = context.consumeIdentifier();

		if (context.tryConsume(RENAME, TO)) {
			alter = new RenameTo(context.consumeIdentifier());
		} else {
			context.consume(ADD);
			context.tryConsume(COLUMN);
			alter = new AddColumn(ColumnDefinition.parse(context));
		}

		alter.schema = schema;
		alter.table = table;

		return alter;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(ALTER, TABLE);
		schema.ifPresent(sql::appendSchema);
		sql.appendIdentifier(table);
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
			sql.append(RENAME, TO).appendIdentifier(name);
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
			sql.append(ADD, COLUMN).append(column);
		}
	}
}
