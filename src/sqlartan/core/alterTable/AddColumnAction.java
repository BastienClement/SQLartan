package sqlartan.core.alterTable;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import java.sql.SQLException;

/**
 * Created by matthieu.villard on 23.05.2016.
 */
public class AddColumnAction extends AlterColumnAction{

	AddColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	public void executeAction() throws SQLException, ParseException {
		String query = "ALTER TABLE " +table.fullName() + "  ADD COLUMN " + getColumnDefinition();
		table.database().execute(query);
	}
}