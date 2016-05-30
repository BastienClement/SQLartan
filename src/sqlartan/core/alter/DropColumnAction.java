package sqlartan.core.alter;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import java.sql.SQLException;
import java.util.List;

/**
 * An alter action structure representing an action which can drop a column
 * from a table structure.
 */
public class DropColumnAction extends UpdateColumnAction{
	/**
	 * @param table the table
	 * @param column the column
	 * @throws TokenizeException
	 */
	DropColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	/**
	 * Execute the action, drop the column and save changes to database.
	 *
	 * @throws SQLException
	 * @throws ParseException
	 */
	public void executeAction() throws SQLException, ParseException {
		List<ColumnDefinition> columns = getTableDefinition().columns;
		columns.remove(columns.stream().filter(col -> col.name.equals(columnDefinition.name)).findFirst().get());

		update(columns);
	}
}
