package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_altertable.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class AlterTableStatement implements Statement {
	public Optional<String> schema = Optional.empty();
	public String table;

	public static AlterTableStatement parse(ParserContext context) {
		context.consume(ALTER, TABLE);
		AlterTableStatement alter;

		Optional<String> schema = context.optConsumeSchema();
		String table = context.consumeIdentifier();

		if (context.current(RENAME)) {
			alter = RenameTo.parse(context);
		} else {
			alter = AddColumn.parse(context);
		}

		alter.schema = schema;
		alter.table = table;

		return alter;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(ALTER, TABLE)
		   .appendSchema(schema)
		   .appendIdentifier(table);
	}

	/**
	 * ALTER TABLE ... RENAME TO ... ;
	 */
	public static class RenameTo extends AlterTableStatement {
		public String name;

		public static RenameTo parse(ParserContext context) {
			context.consume(RENAME, TO);
			RenameTo rename = new RenameTo();
			rename.name = context.consumeIdentifier();
			return rename;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(RENAME, TO)
			   .appendIdentifier(name);
		}
	}

	/**
	 * ALTER TABLE ... ADD COLUMN ... ;
	 */
	public static class AddColumn extends AlterTableStatement {
		public ColumnDefinition column;

		public static AddColumn parse(ParserContext context) {
			context.consume(ADD, COLUMN);
			AddColumn addColumn = new AddColumn();
			addColumn.column = ColumnDefinition.parse(context);
			return addColumn;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(ADD, COLUMN)
			   .append(column);
		}
	}
}
