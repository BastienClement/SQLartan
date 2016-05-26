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
 *
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public abstract class QueryResolver {
	private static boolean columnsAreValid(SelectStatement.Simple select) {
		return select.columns.stream().allMatch(col ->
			match(col).when(ResultColumn.Wildcard.class, wc -> true)
			          .when(ResultColumn.Expr.class, exp -> exp.expression instanceof Expression.ColumnReference)
			          .orElse(false)
		);
	}

	private static Optional<Table> resolveTable(Database database, QualifiedTableName source) {
		return source.schema.filter(n -> !n.equals("main"))
		                    .map(n -> database.attached(n).map(db -> (Database) db))
		                    .orElseGet(() -> Optional.of(database))
		                    .flatMap(db -> db.table(source.name));
	}

	private static Stream<TableColumn> columnAsStream(Table table, Expression expr) {
		Expression.ColumnReference ref = (Expression.ColumnReference) expr;
		return Stream.of(table.column(ref.column).get());
	}

	private static Function<ResultColumn, Stream<TableColumn>> columnAdapter(Table table) {
		return col -> match(col).when(ResultColumn.Expr.class, e -> columnAsStream(table, e.expression))
		                        .when(ResultColumn.Wildcard.class, wc -> table.columns())
		                        .orElseThrow();
	}

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
