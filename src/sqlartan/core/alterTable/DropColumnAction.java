package sqlartan.core.alterTable;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by matthieu.villard on 23.05.2016.
 */
public class DropColumnAction extends UpdateColumnAction{

	DropColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	public void executeAction() throws SQLException, ParseException {
		List<ColumnDefinition> columns = getTableDefinition().columns;
		columns.remove(columns.stream().filter(col -> col.name.equals(columnDefinition.name)).findFirst().get());

		update(columns);
	}
}