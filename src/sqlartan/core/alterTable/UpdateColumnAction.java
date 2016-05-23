package sqlartan.core.alterTable;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by matthieu.villard on 23.05.2016.
 */
public abstract class UpdateColumnAction extends AlterColumnAction{

	UpdateColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table, column);
	}

	protected void update(List<ColumnDefinition> columns) throws SQLException, ParseException {
		CreateTableStatement.Def definition = getTableDefinition();
		definition.columns = columns;
		update(definition);
	}
}