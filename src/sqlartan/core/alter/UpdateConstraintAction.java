package sqlartan.core.alter;

import sqlartan.core.Table;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.TableConstraint;
import sqlartan.core.ast.parser.ParseException;
import java.sql.SQLException;
import java.util.List;

/**
 * TODO
 */
public abstract class UpdateConstraintAction extends AlterAction {
	/**
	 * @param table
	 */
	public UpdateConstraintAction(Table table) {
		super(table);
	}

	/**
	 * TODO
	 *
	 * @param constraints
	 * @throws SQLException
	 * @throws ParseException
	 */
	protected void update(List<TableConstraint> constraints) throws SQLException, ParseException {
		CreateTableStatement.Def definition = getTableDefinition();
		definition.constraints = constraints;
		update(definition);
	}
}
