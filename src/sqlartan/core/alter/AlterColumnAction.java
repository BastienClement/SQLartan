package sqlartan.core.alter;

import sqlartan.core.Index;
import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnConstraint;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.TypeDefinition;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.TokenSource;
import sqlartan.core.ast.token.TokenizeException;
import java.util.Optional;

/**
 * An alter action structure allowing to modify a column structure.
 */
public abstract class AlterColumnAction extends AlterAction {
	/**
	 * The column to modify
	 */
	private final TableColumn column;

	/**
	 * The column definition, generated from the column
	 */
	protected final ColumnDefinition columnDefinition;

	/**
	 * @param table  the table
	 * @param column the column
	 * @throws TokenizeException
	 */
	public AlterColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table);
		this.column = column;
		columnDefinition = createDefinition();
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	public TableColumn column() {
		return column;
	}

	/**
	 * Gets the column definition.
	 *
	 * @return the column definition
	 */
	public ColumnDefinition columnDefinition() {
		return columnDefinition;
	}

	/**
	 * Generates column definition.
	 *
	 * @return the column definition
	 *
	 * @throws TokenizeException
	 */
	private ColumnDefinition createDefinition() throws TokenizeException {
		ColumnDefinition definition = new ColumnDefinition();
		definition.name = column.name();

		TypeDefinition type = new TypeDefinition();
		type.name = column.affinity().name();

		definition.type = Optional.of(type);

		if (column.unique()) {
			definition.constraints.add(new ColumnConstraint.Unique());
		}

		if (!column.nullable()) {
			definition.constraints.add(new ColumnConstraint.NotNull());
		}

		if (column.check().isPresent()) {
			TokenSource source = TokenSource.from(column.check().get());
			ParserContext context = new ParserContext(source);
			definition.constraints.add(ColumnConstraint.Check.parse(context));
		}

		Optional<Index> pk = column.table().primaryKey();
		if (pk.isPresent() && pk.get().columns().contains(this) && pk.get().columns().size() == 1) {
			ColumnConstraint.PrimaryKey constraint = new ColumnConstraint.PrimaryKey();
			constraint.name = Optional.of(pk.get().name());
			constraint.autoincrement = false;
			definition.constraints.add(constraint);
		}

		return definition;
	}
}
