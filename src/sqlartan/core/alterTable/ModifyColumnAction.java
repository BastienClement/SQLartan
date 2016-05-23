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
public class ModifyColumnAction extends UpdateColumnAction{

	private final String originalName;

	ModifyColumnAction(Table table, TableColumn column, String originalName) throws TokenizeException {
		super(table, column);
		this.originalName = originalName;
	}

	public void executeAction() throws ParseException, SQLException {
		List<ColumnDefinition> columns = getTableDefinition().columns;
		ColumnDefinition definition = columns.stream().filter(col -> col.name.equals(columnDefinition.name)).findFirst().get();
		columns.set(columns.indexOf(definition), getColumnDefinition());

		update(columns);
	}
}