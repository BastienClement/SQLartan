package sqlartan.core.alter;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import java.sql.SQLException;
import java.util.List;

/**
 * TODO
 */
public class DropColumnAction extends UpdateColumnAction{

	/**
	 * TODO
	 *
	 * @param table
	 * @param column
	 * @throws TokenizeException
	 */
	DropColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	/**
	 * TODO
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
