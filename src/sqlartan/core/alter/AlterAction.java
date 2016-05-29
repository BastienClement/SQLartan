package sqlartan.core.alter;

import sqlartan.core.Row;
import sqlartan.core.Table;
import sqlartan.core.Trigger;
import sqlartan.core.ast.*;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO
 */
public abstract class AlterAction {
	/**
	 * TODO
	 */
	protected final Table table;

	/**
	 * TODO
	 */
	private CreateTableStatement.Def oldTableDefinition;

	/**
	 * TODO
	 *
	 * @param table
	 */
	public AlterAction(Table table) {
		this.table = table;
	}

	/**
	 * TODO
	 *
	 * @throws SQLException
	 * @throws ParseException
	 */
	public void execute() throws SQLException, ParseException {
		executeAction();
	}

	/**
	 * TODO
	 *
	 * @throws SQLException
	 * @throws ParseException
	 */
	protected abstract void executeAction() throws SQLException, ParseException;

	/**
	 * TODO
	 *
	 * @param tableDefinition
	 * @throws SQLException
	 * @throws ParseException
	 */
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
			definition.columns.stream().filter(col -> definition.columns.indexOf(col) < temporaryTable.columns.size()).map(col -> col.name).collect(Collectors.joining(", ")) +
			" FROM " + table.fullName();

		String dropTable = "DROP TABLE " + table.fullName();

		tableDefinition.schema = Optional.of(table.database().name());
		String createTable = tableDefinition.toSQL();

		String populateTable = "INSERT INTO " + table.fullName() + " SELECT " +
			temporaryTable.columns.stream().map(col -> col.name).collect(Collectors.joining(", ")) +
			" FROM " + temporaryTable.name;

		String dropTemporary = "DROP TABLE " + temporaryTable.name;

		oldTableDefinition = getTableDefinition();

		List<Trigger> triggers = table.triggers().toList();

		table.database().executeTransaction(new String[] { createTemporary, populateTemporary, dropTable, createTable, populateTable, dropTemporary });

		for (Trigger trigger : triggers) {
			updateTrigger(trigger);
		}
	}

	/**
	 * TODO
	 *
	 * @throws SQLException
	 * @throws ParseException
	 */
	public CreateTableStatement.Def getTableDefinition() throws SQLException, ParseException {
		String createStatement = table.database().assemble("SELECT sql FROM ", table.database().name(), ".sqlite_master WHERE type = 'table' AND name = ?")
		                              .execute(table.name())
		                              .mapFirst(Row::getString);

		Parser.parse(createStatement, CreateTableStatement::parse);
		return (CreateTableStatement.Def) Parser.parse(createStatement, CreateTableStatement::parse);
	}

	/**
	 * TODO
	 *
	 * @param trigger
	 * @throws SQLException
	 * @throws ParseException
	 */
	private void updateTrigger(Trigger trigger) throws SQLException, ParseException {
		CreateTriggerStatement definition = Parser.parse(trigger.getContent(), CreateTriggerStatement::parse);

		definition.columns.forEach(col -> {
			try {
				StringBuilder column = new StringBuilder();
				column.append(col);
				if (exist(column)) {
					col = column.toString();
				} else {
					definition.columns.remove(col);
				}
			} catch (SQLException | ParseException e) {
				throw new UncheckedSQLException(e);
			}
		});

		if (definition.when.isPresent()) {
			if (!parseExpression(definition.when.get())) {
				definition.when = Optional.empty();
			}
		}

		Iterator<Statement> iterator = definition.body.iterator();
		while (iterator.hasNext()) {
			if (!parseStatement(iterator.next())) {
				iterator.remove();
			}
		}

		if (!definition.body.isEmpty()) {
			table.database().execute(definition.toSQL());
		}
	}

	/**
	 * TODO
	 *
	 * @param statement
	 * @throws SQLException
	 * @throws ParseException
	 */
	private boolean parseStatement(Statement statement) throws SQLException, ParseException {
		if (statement instanceof UpdateStatement) {
			((UpdateStatement) statement).set.forEach(set -> {
				try {
					StringBuilder column = new StringBuilder();
					column.append(set.column);
					if (exist(column) & parseExpression(set.value)) {
						set.column = column.toString();
					} else {
						((UpdateStatement) statement).set.remove(set);
					}
				} catch (SQLException | ParseException e) {
					throw new UncheckedSQLException(e);
				}

			});
			if (((UpdateStatement) statement).set.isEmpty()) {
				return false;
			}
		} else if (statement instanceof InsertStatement) {
			Iterator<String> iterator = ((InsertStatement) statement).columns.iterator();
			while (iterator.hasNext()) {
				String col = iterator.next();
				StringBuilder column = new StringBuilder();
				column.append(col);
				if (exist(column)) {
					col = column.toString();
				} else {
					iterator.remove();
				}
			}
			if (((InsertStatement) statement).columns.isEmpty()) {
				return false;
			}

			if (statement instanceof InsertStatement.Select) {
				if (!parseSelectStatement(((InsertStatement.Select) statement).select)) {
					return false;
				}
			}
		} else if (statement instanceof DeleteStatement) {
			DeleteStatement delete = (DeleteStatement) statement;
			if (delete.where.isPresent()) {
				if (!parseExpression(delete.where.get().expression)) {
					delete.where = Optional.empty();
				}
			}
			if (delete.orderBy.isPresent()) {
				delete.orderBy.get().terms.forEach(term -> {
					try {
						if (!parseExpression(term.expression)) {
							delete.orderBy.get().terms.remove(term);
						}
					} catch (SQLException | ParseException e) {
						throw new UncheckedSQLException(e);
					}
				});
				if (delete.orderBy.get().terms.isEmpty()) {
					delete.orderBy = Optional.empty();
				}
			}
			if (delete.limit.isPresent()) {
				if (!parseExpression(delete.limit.get().expression)) {
					delete.limit = Optional.empty();
				}
				if (delete.limit.isPresent() && delete.limit.get().offset.isPresent()) {
					if (!parseExpression(delete.limit.get().offset.get())) {
						delete.limit.get().offset = Optional.empty();
					}
				}
			}
		} else if (statement instanceof SelectStatement) {
			if (!parseSelectStatement((SelectStatement) statement)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * TODO
	 *
	 * @param select
	 * @throws SQLException
	 * @throws ParseException
	 */
	private boolean parseSelectStatement(SelectStatement select) throws SQLException, ParseException {
		if (select instanceof ValuesStatement) {
			ValuesStatement valuesStatement = (ValuesStatement) select;

			Iterator<List<Expression>> iterator = valuesStatement.values.iterator();
			while (iterator.hasNext()) {
				List<Expression> values = iterator.next();
				Iterator<Expression> it = values.iterator();
				while (it.hasNext()) {
					if (!parseExpression(it.next())) {
						it.remove();
					}
				}
				if (values.isEmpty()) {
					iterator.remove();
				}
			}
			if (valuesStatement.values.isEmpty()) {
				return false;
			}
		} else if (select instanceof SelectStatement.Simple) {
			SelectStatement.Simple simple = (SelectStatement.Simple) select;

			simple.columns.stream().filter(column -> column instanceof ResultColumn.Expr).forEach(column -> {
				try {
					if (!parseExpression(((ResultColumn.Expr) column).expression)) {
						simple.columns.remove(column);
					}
				} catch (SQLException | ParseException e) {
					throw new UncheckedSQLException(e);
				}
			});
			if (simple.columns.isEmpty())
				return false;

			if (simple.from.isPresent()) {
				if (!parseSelectSource(simple.from.get())) {
					simple.from = Optional.empty();
				}
			}

			if (simple.where.isPresent()) {
				if (!parseExpression(simple.where.get().expression)) {
					simple.where = Optional.empty();
				}
			}

			simple.groupBy.forEach(exp -> {
				try {
					if (!parseExpression(exp)) {
						simple.groupBy.remove(exp);
					}
				} catch (SQLException | ParseException e) {
					throw new UncheckedSQLException(e);
				}
			});


			if (simple.having.isPresent()) {
				if (!parseExpression(simple.having.get())) {
					simple.having = Optional.empty();
				}
			}

			if (simple.orderBy.isPresent()) {
				simple.orderBy.get().terms.forEach(term -> {
					try {
						if (!parseExpression(term.expression)) {
							simple.orderBy.get().terms.remove(term);
						}
					} catch (SQLException e) {
						throw new UncheckedSQLException(e);
					} catch (ParseException e) {
						throw new UncheckedSQLException(e);
					}
				});
				if (simple.orderBy.get().terms.isEmpty()) {
					simple.orderBy = Optional.empty();
				}
			}

			if (simple.limit.isPresent()) {
				if (!parseExpression(simple.limit.get().expression)) {
					simple.limit = Optional.empty();
				}
				if (simple.limit.isPresent() && simple.limit.get().offset.isPresent()) {
					if (!parseExpression(simple.limit.get().offset.get())) {
						simple.limit.get().offset = Optional.empty();
					}
				}
			}
		} else if (select instanceof CompoundSelectStatement) {
			CompoundSelectStatement compound = (CompoundSelectStatement) select;

			if (compound.orderBy.isPresent()) {
				compound.orderBy.get().terms.forEach(term -> {
					try {
						if (!parseExpression(term.expression)) {
							compound.orderBy.get().terms.remove(term);
						}
					} catch (SQLException e) {
						throw new UncheckedSQLException(e);
					} catch (ParseException e) {
						throw new UncheckedSQLException(e);
					}
				});
				if (compound.orderBy.get().terms.isEmpty()) {
					compound.orderBy = Optional.empty();
				}
			}

			if (compound.limit.isPresent()) {
				if (!parseExpression(compound.limit.get().expression)) {
					compound.limit = Optional.empty();
				}
				if (compound.limit.isPresent() && compound.limit.get().offset.isPresent()) {
					if (!parseExpression(compound.limit.get().offset.get())) {
						compound.limit.get().offset = Optional.empty();
					}
				}
			}
			return parseSelectStatement(compound.lhs) && parseSelectStatement(compound.rhs);
		}
		return true;
	}

	/**
	 * TODO
	 *
	 * @param source
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	private boolean parseSelectSource(SelectSource source) throws SQLException, ParseException {
		if (source instanceof SelectSource.Function) {
			((SelectSource.Function) source).args.forEach(arg -> {
				try {
					if (!parseExpression(arg)) {
						((SelectSource.Function) source).args.remove(arg);
					}
				} catch (SQLException | ParseException e) {
					throw new UncheckedSQLException(e);
				}
			});
			if (((SelectSource.Function) source).args.isEmpty())
				return false;
		} else if (source instanceof SelectSource.Group) {
			return parseSelectSource(((SelectSource.Group) source).source);
		} else if (source instanceof SelectSource.Subquery) {
			return parseSelectStatement(((SelectSource.Subquery) source).query);
		}
		return true;
	}

	/**
	 * TODO
	 *
	 * @param exp
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	private boolean parseExpression(Expression exp) throws SQLException, ParseException {
		if (exp instanceof Expression.BinaryOperator) {
			Expression.BinaryOperator op = (Expression.BinaryOperator) exp;
			return parseExpression(op.lhs) && parseExpression(op.rhs);
		} else if (exp instanceof Expression.ColumnReference) {
			StringBuilder column = new StringBuilder();
			column.append(((Expression.ColumnReference) exp).column);
			if (exist(column)) {
				((Expression.ColumnReference) exp).column = column.toString();
				return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * TODO
	 *
	 * @param column
	 * @throws SQLException
	 * @throws ParseException
	 */
	private boolean exist(final StringBuilder column) throws SQLException, ParseException {
		CreateTableStatement.Def tableDefinition = getTableDefinition();
		final String finalColumn = column.toString();
		if (tableDefinition.columns.stream().filter(col -> col.name.equals(column.toString())).findFirst().isPresent()) {
			return true;
		}
		if (tableDefinition.columns.size() == oldTableDefinition.columns.size() && oldTableDefinition.columns.stream().filter(col -> col.name.equals(finalColumn)).findFirst().isPresent()) {
			column.delete(0, column.length());
			column.append(tableDefinition.columns.get(oldTableDefinition.columns.indexOf(oldTableDefinition.columns.stream().filter(col -> col.name.equals(finalColumn)).findFirst().get())).name);
			return true;
		}

		return false;
	}
}
