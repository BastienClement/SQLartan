package sqlartan.core;

import org.junit.Test;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

public class TableTests {
	@Test
	public void tablesListTests() throws SQLException {
		try (Database db = new Database()) {
			db.execute("CREATE TABLE a (z INT)");
			db.execute("CREATE TABLE c (z INT)");
			db.execute("CREATE TABLE b (z INT)");
			db.execute("CREATE VIEW z AS SELECT * FROM a");

			List<String> names = db.tables().map(Table::name).collect(Collectors.toList());
			assertEquals(Arrays.asList("a", "b", "c"), names);

			Optional<Table> ta = db.table("a");
			Optional<Table> tz = db.table("z");

			assertTrue(ta.isPresent());
			assertFalse(tz.isPresent());
		}
	}

	@Test
	public void tablesColumnsTests() throws SQLException {
		try (Database db = new Database()) {
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			List<TableColumn> columns = test.columns().toList();
			assertEquals(3, columns.size());

			List<String> names = Arrays.asList("a", "b", "c");
			assertEquals(names, columns.stream().map(Column::name).collect(Collectors.toList()));

			List<String> types = Arrays.asList("INT", "TEXT", "FLOAT");
			assertEquals(types, columns.stream().map(Column::type).collect(Collectors.toList()));

			List<Affinity> affinities = Arrays.asList(Affinity.Integer, Affinity.Text, Affinity.Real);
			assertEquals(affinities, columns.stream().map(Column::affinity).collect(Collectors.toList()));
		}
	}

	@Test
	public void duplicateTableTests() throws SQLException {
		try (Database db = new Database()) {
			// Create and duplicate a simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO test VALUES (1, 'abc', 11)");
			db.execute("INSERT INTO test VALUES (2, 'def', 12)");
			db.execute("INSERT INTO test VALUES (3, 'ghi', 13)");
			Table test = db.table("test").get();
			test.duplicate("test2");

			// Check if the structure is right
			Table test2 = db.table("test2").get();
			List<TableColumn> columns = test2.columns().toList();
			assertEquals(3, columns.size());
			List<String> names = Arrays.asList("a", "b", "c");
			assertEquals(names, columns.stream().map(Column::name).collect(Collectors.toList()));

			// Check if we have exactly 3 rows in the table
			int count = db.execute("SELECT COUNT(*) FROM test2").mapFirst(Row::getInt);
			assertTrue(count == 3);

			// Create and duplicate a table which name is contained inside the keyword CREATE
			db.execute("CREATE TABLE EA (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO EA VALUES (1, 'abc', 11)");
			db.execute("INSERT INTO EA VALUES (2, 'def', 12)");
			db.execute("INSERT INTO EA VALUES (3, 'ghi', 13)");
			Table ea = db.table("EA").get();
			ea.duplicate("EA2");

			// Check if the structure is right
			Table ea2 = db.table("EA2").get();
			List<TableColumn> columns2 = ea2.columns().toList();
			assertEquals(3, columns.size());
			List<String> names2 = Arrays.asList("a", "b", "c");
			assertEquals(names2, columns.stream().map(Column::name).collect(Collectors.toList()));

			// Check if we have exactly 3 rows in the table
			int count2 = db.execute("SELECT COUNT(*) FROM EA2").mapFirst(Row::getInt);
			assertTrue(count == 3);
		}
	}
}
