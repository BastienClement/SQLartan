package sqlartan.core.alter;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.*;
import sqlartan.core.ast.parser.ParseException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO
 */
public class PrimaryKeyAction extends AlterAction {
	/**
	 * TODO
	 */
	List<TableColumn> columns;

	/**
	 * @param table
	 * @param columns
	 */
	public PrimaryKeyAction(Table table, List<TableColumn> columns) {
		super(table);
		this.columns = columns;
	}

	/**
	 * TODO
	 *
	 * @throws SQLException
	 * @throws ParseException
	 */
	@Override
	protected void executeAction() throws SQLException, ParseException {
		CreateTableStatement.Def definition = getTableDefinition();
		Optional<ColumnDefinition> columnPk = definition.columns.stream().filter(columnDefinition -> columnDefinition.constraints.stream().filter(constraint -> constraint instanceof ColumnConstraint.PrimaryKey).findFirst().isPresent()).findFirst();
		if (columnPk.isPresent()) {
			columnPk.get().constraints.remove(columnPk.get().constraints.stream().filter(constraint -> constraint instanceof ColumnConstraint.PrimaryKey).findFirst().get());
		}
		if (columns.size() == 1) {
			ColumnDefinition column = definition.columns.stream().filter(col -> col.name.equals(columns.get(0).name())).findFirst().get();
			ColumnConstraint.PrimaryKey constraint = new ColumnConstraint.PrimaryKey();
			constraint.autoincrement = false;
			column.constraints.add(constraint);
		} else {
			List<IndexedColumn> indexedColumns;
			Optional<TableConstraint> constraint = definition.constraints.stream().filter(tableConstraint -> tableConstraint instanceof TableConstraint.Index && ((TableConstraint.Index) tableConstraint).type == TableConstraint.Index.Type.PrimaryKey).findFirst();
			if (constraint.isPresent()) {
				indexedColumns = ((TableConstraint.Index) constraint.get()).columns;
				indexedColumns.clear();
			} else {
				TableConstraint.Index pk = new TableConstraint.Index();
				pk.type = TableConstraint.Index.Type.PrimaryKey;
				indexedColumns = new ArrayList<>();
				pk.columns = indexedColumns;
			}

			for (TableColumn column : columns) {
				Expression.ColumnReference ref = new Expression.ColumnReference();
				ref.column = column.name();
				ref.table = Optional.of(definition.name);
				ref.schema = Optional.empty();
				IndexedColumn indexedColumn = new IndexedColumn();
				indexedColumn.expression = ref;
			}
		}

		update(definition);
	}
}
