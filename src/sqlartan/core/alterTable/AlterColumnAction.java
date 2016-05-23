package sqlartan.core.alterTable;

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
 * Created by matthieu.villard on 23.05.2016.
 */
public abstract class AlterColumnAction extends AlterAction
{
	private final TableColumn column;
	protected final ColumnDefinition columnDefinition;

	public AlterColumnAction(Table table, TableColumn column) throws TokenizeException {
		super(table);
		this.column = column;
		columnDefinition = createDefinition();
	}

	public TableColumn column(){
		return column;
	}

	public ColumnDefinition getColumnDefinition(){
		return columnDefinition;
	}

	private ColumnDefinition createDefinition() throws TokenizeException {
		ColumnDefinition definition = new ColumnDefinition();
		definition.name = column.name();

		TypeDefinition type = new TypeDefinition();
		type.name = column.affinity().name();

		definition.type = Optional.of(type);

		if(column.unique()){
			definition.constraints.add(new ColumnConstraint.Unique());
		}

		if(!column.nullable()){
			definition.constraints.add(new ColumnConstraint.NotNull());
		}

		if(column.check().isPresent()){
			TokenSource source = TokenSource.from(column.check().get());
			ParserContext context = new ParserContext(source);
			definition.constraints.add(ColumnConstraint.Check.parse(context));
		}

		Index pk = column.parentTable().primaryKey();
		if(pk.getColumns().contains(this) && pk.getColumns().size() == 1){
			ColumnConstraint.PrimaryKey constraint = new ColumnConstraint.PrimaryKey();
			constraint.name = Optional.of(pk.getName());
			constraint.autoincrement = false;
			definition.constraints.add(constraint);
		}

		return definition;
	}
}