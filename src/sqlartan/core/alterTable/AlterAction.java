package sqlartan.core.alterTable;

import sqlartan.core.Row;
import sqlartan.core.Table;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.CreateTriggerStatement;
import sqlartan.core.ast.CreateViewStatement;
import sqlartan.core.ast.SelectStatement;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.stream.IterableStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by matthieu.villard on 23.05.2016.
 */
public abstract class AlterAction
{
	protected final Table table;

	public AlterAction(Table table){
		this.table = table;
	}

	public void execute() throws SQLException, ParseException {
		executeAction();
	}

	protected abstract void executeAction() throws SQLException, ParseException;

	protected void update(CreateTableStatement.Def tableDefinition) throws SQLException, ParseException {
		CreateTableStatement.Def temporaryTable = new CreateTableStatement.Def();
		temporaryTable.columns = tableDefinition.columns;
		temporaryTable.name = table.name() + "_backup";
		temporaryTable.constraints = tableDefinition.constraints;
		temporaryTable.temporary = true;
		temporaryTable.schema = Optional.empty();

		String createTemporary = temporaryTable.toSQL();

		CreateTableStatement.Def definition = getTableDefinition();

		String populateTemporary = "INSERT INTO " + temporaryTable.name + " SELECT " +
			definition.columns.stream().filter(col -> temporaryTable.columns.stream().filter(c -> c.name.equals(col.name)).findFirst().isPresent()).map(col -> col.name).collect(Collectors.joining(", ")) +
			" FROM " + table.fullName();

		String dropTable = "DROP TABLE " + table.fullName();

		tableDefinition.schema = Optional.of(table.database().name());
		String createTable = tableDefinition.toSQL();

		String populateTable = "INSERT INTO " + table.fullName() + " SELECT " +
			temporaryTable.columns.stream().map(col -> col.name).collect(Collectors.joining(", ")) +
			" FROM " + temporaryTable.name;

		String dropTemporary = "DROP TABLE " + temporaryTable.name;

		List<CreateTriggerStatement> triggerDefinitions = getTriggersDefinitions();
		table.database().executeTransaction(new String[]{createTemporary, populateTemporary, dropTable, createTable, populateTable, dropTemporary});

		triggerDefinitions.stream().forEach(def -> {
			def.columns.stream().filter(colName -> !tableDefinition.columns.stream().filter(col -> col.name.equals(colName)).findFirst().isPresent()).forEach(col -> def.columns.remove(col));
			try {
				table.database().execute(def.toSQL());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public CreateTableStatement.Def getTableDefinition() throws SQLException, ParseException {
		String createStatement = table.database().assemble("SELECT sql FROM ", table.database().name(), ".sqlite_master WHERE type = 'table' AND name = ?")
		                                       .execute(table.name())
		                                       .mapFirst(Row::getString);

		Parser.parse(createStatement, CreateTableStatement::parse);
		return (CreateTableStatement.Def)Parser.parse(createStatement, CreateTableStatement::parse);
	}

	public List<CreateTriggerStatement> getTriggersDefinitions() throws ParseException, SQLException {
		List<CreateTriggerStatement> definitions = new ArrayList<>();
		Iterator<String> iterator = table.triggers().keySet().iterator();
		while (iterator.hasNext()){
			String createStatement = table.database().assemble("SELECT name, sql, tbl_name FROM ", table.database().name(), ".sqlite_master WHERE type = 'trigger' AND tbl_name = ? AND name = ?")
			                                       .execute(table.name(), iterator.next())
			                                       .mapFirst(Row::getString);
			definitions.add(Parser.parse(createStatement, CreateTriggerStatement::parse));
		}

		return definitions;
	}

	public List<CreateViewStatement> getViewsDefinitions() throws SQLException {
		IterableStream<String> createStatements =  table.database().assemble("SELECT sql FROM ", table.database().name(), ".sqlite_master WHERE type = 'view'")
		                                                         .execute()
		                                                         .map(Row::getString);

		return createStatements.map(createStatement -> {
			try {
				return Parser.parse(createStatement, CreateViewStatement::parse);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}).filter(createViewStatement -> {
			if(createViewStatement.as instanceof SelectStatement.Simple){
				SelectStatement.Simple select = (SelectStatement.Simple)createViewStatement.as;
				if(select.from.get().alias.get().equals(table.name()))
					return true;
			}
			return false;
		}).toList();
	}
}