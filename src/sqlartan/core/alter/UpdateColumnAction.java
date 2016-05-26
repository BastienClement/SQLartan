package sqlartan.core.alter;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import java.sql.SQLException;
import java.util.List;

/**
 * TODO
 */
public abstract class UpdateColumnAction extends AlterColumnAction {
	/**
	 * @param table
	 * @param column
	 * @throws TokenizeException
	 */
	UpdateColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	/**
	 * TODO
	 *
	 * @param columns
	 * @throws SQLException
	 * @throws ParseException
	 */
	protected void update(List<ColumnDefinition> columns) throws SQLException, ParseException {
		CreateTableStatement.Def definition = getTableDefinition();
		definition.columns = columns;
		update(definition);
	}
}
