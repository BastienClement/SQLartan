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
 * An alter action structure representing an action on column which would
 * modify table structure.
 */
public abstract class UpdateColumnAction extends AlterColumnAction {
	/**
	 * @param table the table
	 * @param column the column
	 * @throws TokenizeException
	 */
	UpdateColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	/**
	 * Update table structure, based on columns definition.
	 *
	 * @param columns the columns definition
	 * @throws SQLException
	 * @throws ParseException
	 */
	protected void update(List<ColumnDefinition> columns) throws SQLException, ParseException {
		CreateTableStatement.Def definition = getTableDefinition();
		definition.columns = columns;
		update(definition);
	}
}
