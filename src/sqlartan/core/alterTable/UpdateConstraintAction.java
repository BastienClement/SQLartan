package sqlartan.core.alterTable;

import sqlartan.core.Table;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.TableConstraint;
import sqlartan.core.ast.parser.ParseException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by matthieu.villard on 23.05.2016.
 */
public abstract class UpdateConstraintAction extends AlterAction {

	public UpdateConstraintAction(Table table) {
		super(table);
	}

	protected void update(List<TableConstraint> constraints) throws SQLException, ParseException {
		CreateTableStatement.Def definition = getTableDefinition();
		definition.constraints = constraints;
		update(definition);
	}
}
