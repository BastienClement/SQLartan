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
public class ModifyColumnAction extends UpdateColumnAction {
	/**
	 * TODO
	 */
	private final String originalName;

	/**
	 * @param table
	 * @param column
	 * @param originalName
	 * @throws TokenizeException
	 */
	ModifyColumnAction(Table table, TableColumn column, String originalName) throws TokenizeException {
		super(table, column);
		this.originalName = originalName;
	}

	/**
	 * TODO
	 *
	 * @throws ParseException
	 * @throws SQLException
	 */
	@Override
	public void executeAction() throws ParseException, SQLException {
		List<ColumnDefinition> columns = getTableDefinition().columns;
		ColumnDefinition definition = columns.stream().filter(col -> col.name.equals(originalName)).findFirst().get();
		columns.set(columns.indexOf(definition), getColumnDefinition());

		update(columns);
	}
}
