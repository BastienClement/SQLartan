package sqlartan.core.util;

import sqlartan.core.Database;
import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.Expression;
import sqlartan.core.ast.QualifiedTableName;
import sqlartan.core.ast.ResultColumn;
import sqlartan.core.ast.SelectStatement;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.stream.ImmutableList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import static sqlartan.util.Matching.match;

/**
 * Utility class for resolving query columns.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public abstract class QueryResolver {
	/**
	 * Checks that columns of a simple select statement are valid for
	 * resolution. Columns are valid if they are all either wildcard (*)
	 * or expression composed of column references.
	 *
	 * @param select the simple select statement
	 * @return true if all columns are valid
	 */
	private static boolean columnsAreValid(SelectStatement.Simple select) {
		return select.columns.stream().allMatch(col ->
			match(col).when(ResultColumn.Wildcard.class, wc -> true)
			          .when(ResultColumn.Expr.class, exp -> exp.expression instanceof Expression.ColumnReference)
			          .orElse(false)
		);
	}

	/**
	 * Resolves a qualified table name to a Table object.
	 *
	 * @param database the source database
	 * @param table    the qualified table name
	 * @return the corresponding Table object, if it exists
	 */
	private static Optional<Table> resolveTable(Database database, QualifiedTableName table) {
		return table.schema.filter(n -> !n.equals("main"))
		                   .map(n -> database.attached(n).map(db -> (Database) db))
		                   .orElseGet(() -> Optional.of(database))
		                   .flatMap(db -> db.table(table.name));
	}

	/**
	 * Transforms a column reference expression to a stream of one TableColumn.
	 *
	 * @param table the source table
	 * @param expr  the column reference expression
	 * @return a stream of one table column
	 */
	private static Stream<TableColumn> columnAsStream(Table table, Expression expr) {
		Expression.ColumnReference ref = (Expression.ColumnReference) expr;
		return Stream.of(table.column(ref.column).get());
	}

	/**
	 * Creates a adapter function transforming ResultColumn to a stream of
	 * TableColumm.
	 *
	 * @param table the source table
	 * @return a function transforming ResultColumn to a stream of TableColumn
	 */
	private static Function<ResultColumn, Stream<TableColumn>> columnAdapter(Table table) {
		return col -> match(col).when(ResultColumn.Expr.class, e -> columnAsStream(table, e.expression))
		                        .when(ResultColumn.Wildcard.class, wc -> table.columns())
		                        .orElseThrow();
	}

	/**
	 * Given a database, creates an adapter function that transform the a
	 * simple select statement to an optional immutable list of TableColumn.
	 *
	 * @param database the source database
	 * @return an adapter function that extracts columns from a select
	 */
	private static Function<SelectStatement.Simple, Optional<ImmutableList<TableColumn>>> injectColumns(Database database) {
		return select -> {
			try {
				return resolveTable(database, (QualifiedTableName) select.from.get()).map(t ->
					ImmutableList.from(select.columns.stream().flatMap(columnAdapter(t)))
				);
			} catch (NoSuchElementException ignored) {
				return Optional.empty();
			}
		};
	}

	/**
	 * Resolves references to TableColumns from a select statement.
	 * <p>
	 * This can only be done if the select is a simple select statement,
	 * with no compound operators and exactly one source table. In addition,
	 * the query must use only non-scoped wildcards ('*') and simple
	 * column references.
	 * <p>
	 * It is not possible to resolve columns of a select statement using
	 * the sqlite_master table. Allowing this would create an infinite
	 * loop since this function needs to query the table structure.
	 *
	 * @param database the source database on which the query is executed
	 * @param sql      the query SQL source
	 * @return a list of table columns, if the operation is successful
	 */
	public static Optional<ImmutableList<TableColumn>> resolveColumns(Database database, String sql) {
		return Parser.tryParse(sql, SelectStatement::parse)
		             .filter(s -> s instanceof SelectStatement.Simple)
		             .map(s -> (SelectStatement.Simple) s)
		             .filter(s -> s.from.orElse(null) instanceof QualifiedTableName)
		             .filter(s -> s.from.map(tn -> !((QualifiedTableName) tn).name.equals("sqlite_master")).get())
		             .filter(QueryResolver::columnsAreValid)
		             .flatMap(injectColumns(database));
	}
}
