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

			// TODO add triggers copy tests
		}
	}

	@Test
	public void renameTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			// Rename table
			test.rename("test2");
			// check fields
			assertEquals(test.name(), "test2");

			// Check if table exists
			Table test2 = db.table("test2").get();
			assertNotNull(test2);
			assertEquals(test.name(), "test2");

			// Check that there is no table with the old name
			assertFalse(db.table("test").isPresent());
		}
	}

	@Test
	public void columnTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			// Check that the columns exist
			assertTrue(test.column("a").isPresent());
			assertTrue(test.column("b").isPresent());
			assertTrue(test.column("c").isPresent());

			// Retrieve the columns
			TableColumn colA = test.column("a").get();
			TableColumn colB = test.column("b").get();
			TableColumn colC = test.column("c").get();

			// Check unique constraints
			assertTrue(colA.unique());
			assertTrue(colB.unique());
			assertFalse(colC.unique());

			// Check columns names
			assertEquals(colA.name(), "a");
			assertEquals(colB.name(), "b");
			assertEquals(colC.name(), "c");

			// Check columns types
			assertEquals(colA.type(), "INT");
			assertEquals(colB.type(), "TEXT");
			assertEquals(colC.type(), "FLOAT");

			// Check columns indexes
			assertEquals(test.column(0).get().name(), "a");
			assertEquals(test.column(1).get().name(), "b");
			assertEquals(test.column(2).get().name(), "c");
		}
	}

	@Test
	public void dropTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			// Delete table
			test.drop();

			// Check that the table has been removed
			assertFalse(db.table("test").isPresent());
		}
	}

	@Test
	public void pkTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			// Gets the primary key
			Index pk = test.primaryKey();
			// Check that there is a primary key
			assertNotNull(pk);
			// Check that the primary key has one column
			assertNotNull(pk.getColumns());
			assertTrue(pk.getColumns().size() == 1);
			// Check the primary key column
			assertEquals(pk.getColumns().get(0), "a");
		}
	}

	@Test
	public void triggerTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			// Create a simple trigger
			db.execute("CREATE TRIGGER trig  AFTER INSERT ON test BEGIN DELETE FROM test; END;");

			// Check that an existing trigger can be found
			assertNull(db.table("test").get().trigger("trigg"));
			assertNotNull(db.table("test").get().trigger("trig"));

			// Rename table
			test.rename("test2");
			// Check that the trigger is linked to this table
			assertNotNull(db.table("test2").get().trigger("trig"));

			// Rename trigger
			db.table("test2").get().trigger("trig").rename("trigg");
			// Check that the old trigger does not exist anymore
			assertNull(db.table("test2").get().trigger("trig"));
			// Check that the new trigger exists
			assertNotNull(db.table("test2").get().trigger("trigg"));

			// Delete trigger
			db.table("test2").get().trigger("trigg").drop();
			// Check that the trigger does not exist anymore
			assertNull(db.table("test2").get().trigger("trig"));
		}
	}

	@Test
	public void truncateTests() throws SQLException{
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO test VALUES (1, 'abc', 11)");
			db.execute("INSERT INTO test VALUES (2, 'def', 12)");
			db.execute("INSERT INTO test VALUES (3, 'ghi', 13)");
			Table test = db.table("test").get();

			// Check if we have exactly 3 rows in the table
			int count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			test.truncate();

			// Check if we have exactly 0 rows in the table
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(0, count);
		}
	}

	/*@Test
	public void alterTests() throws SQLException, ParseException {
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO test VALUES (1, 'abc', 11)");
			db.execute("INSERT INTO test VALUES (2, 'def', 12)");
			db.execute("INSERT INTO test VALUES (3, 'ghi', 13)");
			db.execute("CREATE TABLE test_backup (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO test_backup VALUES (1, 'abc', 11)");
			Table test = db.table("test").get();

			AlterTable alter = test.alter();
			// add
			alter.addColumn(test, new TableColumn.Properties());
			alter.addColumn("d", "FLOAT");
			alter.execute();
			assertTrue(test.column("d").isPresent());
			int count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// drop
			alter.dropColumn("d");
			alter.execute();
			test = db.table("test").get();
			assertFalse(test.column("d").isPresent());
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// modify column
			alter.modifyColumn("c", "d", "FLOAT");
			alter.execute();
			test = db.table("test").get();
			assertFalse(test.column("c").isPresent());
			assertTrue(test.column("d").isPresent());

			// Check if we have exactly 3 rows in the table
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);
		}
	}
	*/
}
