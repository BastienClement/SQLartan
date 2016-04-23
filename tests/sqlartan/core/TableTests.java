package sqlartan.core;

import org.junit.Test;
import sqlartan.core.stream.ImmutableList;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class TableTests {
	@Test
	public void tablesColumnsTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			ImmutableList<TableColumn> columns = test.columns().toList();
			assertEquals(3, columns.size());

			List<String> names = Arrays.asList("a", "b", "c");
			assertEquals(names, columns.map(Column::name));

			List<String> types = Arrays.asList("INT", "TEXT", "FLOAT");
			assertEquals(types, columns.map(Column::type));

			List<Affinity> affinities = Arrays.asList(Affinity.Integer, Affinity.Text, Affinity.Real);
			assertEquals(affinities, columns.map(Column::affinity));
		}
	}

	@Test
	public void duplicateTableTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			// Create and duplicate a simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO test VALUES (1, 'abc', 11)");
			db.execute("INSERT INTO test VALUES (2, 'def', 12)");
			db.execute("INSERT INTO test VALUES (3, 'ghi', 13)");

			Table test = db.table("test").get();
			test.duplicate("test2");

			// Check if the structure is right
			Table test2 = db.table("test2").get();
			ImmutableList<TableColumn> columns = test2.columns().toList();
			assertEquals(3, columns.size());
			List<String> names = Arrays.asList("a", "b", "c");
			assertEquals(names, columns.map(Column::name));

			// Check if we have exactly 3 rows in the table
			int count = db.execute("SELECT COUNT(*) FROM test2").mapFirst(Row::getInt);
			assertEquals(3, count);

			// Create and duplicate a table which name is contained inside the keyword CREATE
			db.execute("CREATE TABLE EA (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO EA VALUES (1, 'abc', 11)");
			db.execute("INSERT INTO EA VALUES (2, 'def', 12)");
			db.execute("INSERT INTO EA VALUES (3, 'ghi', 13)");
			Table ea = db.table("EA").get();
			ea.duplicate("EA2");

			// Check if the structure is right
			Table ea2 = db.table("EA2").get();
			ImmutableList<TableColumn> columns2 = ea2.columns().toList();
			assertEquals(3, columns.size());
			List<String> names2 = Arrays.asList("a", "b", "c");
			assertEquals(names2, columns.map(Column::name));

			// Check if we have exactly 3 rows in the table
			int count2 = db.execute("SELECT COUNT(*) FROM EA2").mapFirst(Row::getInt);
			assertEquals(3, count);
		}
	}
}
