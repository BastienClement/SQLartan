package sqlartan.core.alterTable;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.Expression;
import sqlartan.core.ast.IndexedColumn;
import sqlartan.core.ast.TableConstraint;
import sqlartan.core.ast.parser.ParseException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Created by matthieu.villard on 23.05.2016.
 */
public class PrimaryKeyAction extends AlterAction {

	List<TableColumn> columns;

	public PrimaryKeyAction(Table table, List<TableColumn> columns){
		super(table);
		this.columns = columns;
	}

	@Override
	protected void executeAction() throws SQLException, ParseException {
		CreateTableStatement.Def definition = getTableDefinition();
		List<IndexedColumn> indexedColumns = ((TableConstraint.Index) definition.constraints.stream().filter(tableConstraint -> tableConstraint instanceof TableConstraint.Index && ((TableConstraint.Index)tableConstraint).type == TableConstraint.Index.Type.PrimaryKey).findFirst().get()).columns;
		indexedColumns.clear();
		for(TableColumn column : columns){
			Expression.ColumnReference ref = new Expression.ColumnReference();
			ref.column = column.name();
			ref.table = Optional.of(definition.name);
			ref.schema = Optional.empty();
			IndexedColumn indexedColumn = new IndexedColumn();
			indexedColumn.expression = ref;
		}

		update(definition);
	}
}
