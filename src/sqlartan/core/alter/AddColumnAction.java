package sqlartan.core.alter;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import java.sql.SQLException;

/**
 * TODO
 */
public class AddColumnAction extends AlterColumnAction {
	/**
	 * @param table
	 * @param column
	 * @throws TokenizeException
	 */
	AddColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	/**
	 * TODO
	 *
	 * @throws SQLException
	 * @throws ParseException
	 */
	@Override
	public void executeAction() throws SQLException, ParseException {
		String query = "ALTER TABLE " + table.fullName() + "  ADD COLUMN " + getColumnDefinition().toSQL();
		table.database().execute(query);
	}
}
