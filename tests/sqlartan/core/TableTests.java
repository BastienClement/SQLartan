package sqlartan.core;

import org.junit.Ignore;
import org.junit.Test;
import sqlartan.core.alter.AlterTable;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.stream.ImmutableList;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
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
			Optional<Index> pk = test.primaryKey();
			// Check that there is a primary key
			assertTrue(pk.isPresent());
			// Check that the primary key has one column
			assertNotNull(pk.get().columns());
			assertTrue(pk.get().columns().size() == 1);
			// Check the primary key column
			assertEquals(pk.get().columns().get(0), "a");
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
			assertFalse(db.table("test").get().trigger("trigg").isPresent());
			assertTrue(db.table("test").get().trigger("trig").isPresent());

			// Rename table
			test.rename("test2");
			// Check that the trigger is linked to this table
			assertNotNull(db.table("test2").get().trigger("trig"));

			// Rename trigger
			db.table("test2").get().trigger("trig").get().rename("trigg");
			// Check that the old trigger does not exist anymore
			assertFalse(db.table("test2").get().trigger("trig").isPresent());
			// Check that the new trigger exists
			assertTrue(db.table("test2").get().trigger("trigg").isPresent());

			// Delete trigger
			db.table("test2").get().trigger("trigg").get().drop();
			// Check that the trigger does not exist anymore
			assertFalse(db.table("test2").get().trigger("trigg").isPresent());
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

	@Ignore
	@Test
	public void alterTests() throws SQLException, ParseException {
		try (Database db = Database.createEphemeral()) {
			// Create simple table
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO test VALUES (1, 'abc', 11)");
			db.execute("INSERT INTO test VALUES (2, 'def', 12)");
			db.execute("INSERT INTO test VALUES (3, 'ghi', 13)");
			db.execute("CREATE TABLE test_backup (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			db.execute("INSERT INTO test_backup VALUES (1, 'abc', 11)");
			db.execute("CREATE TRIGGER test_trigger AFTER INSERT ON test BEGIN INSERT INTO test_backup(a, b, c) VALUES(new.a, new.b, new.c); END;");
			Table test = db.table("test").get();

			AlterTable alter = test.alter();
			// add
			TableColumn d = new TableColumn(test, new TableColumn.Properties() {
				@Override
				public boolean unique() {
					return false;
				}
				@Override
				public boolean primaryKey() {
					return false;
				}
				@Override
				public String check() {
					return null;
				}
				@Override
				public String name() {
					return "d";
				}
				@Override
				public String type() {
					return "FLOAT";
				}
				@Override
				public boolean nullable() {
					return true;
				}
			});
			alter.addColumn(d);
			alter.execute();

			test = db.table("test").get();

			assertTrue(test.column("d").isPresent());
			int count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// drop
			alter.dropColumn(d);
			alter.execute();
			test = db.table("test").get();
			assertFalse(test.column("d").isPresent());
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// modify column
			test.column("c").get().rename("d");
			alter.execute();
			test = db.table("test").get();
			assertFalse(test.column("c").isPresent());
			assertTrue(test.column("d").isPresent());

			// Check if we have exactly 3 rows in the table
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// modify column type
			alter.modifyColumn("b", new TableColumn(test, new TableColumn.Properties(){
				@Override
				public String name() {
					return "b";
				}
				@Override
				public String type() {
					return "FLOAT";
				}
				@Override
				public boolean nullable() {
					return true;
				}
				@Override
				public boolean unique() {
					return false;
				}
				@Override
				public boolean primaryKey() {
					return false;
				}
				@Override
				public String check() {
					return null;
				}
			}));

			alter.execute();
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);
			assertFalse(test.column("b").get().type().equals("TEXT"));


			// modify primary key
			List<TableColumn> list = new ArrayList<>();
			list.add(test.column("d").get());
			alter.setPrimaryKey(list);
			alter.execute();

			test = db.table("test").get();
			Optional<Index> pk = test.primaryKey();
			assertTrue(pk.isPresent() && pk.get().columns().size() == 1 && pk.get().columns().get(0).equals("d"));

			alter.execute();
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// drop primary key
			list = new ArrayList<>();
			alter.setPrimaryKey(list);
			alter.execute();

			test = db.table("test").get();
			pk = test.primaryKey();
			assertFalse(pk.isPresent());

			alter.execute();
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// drop column b
			alter.dropColumn(test.column("b").get());
			alter.execute();
			test = db.table("test").get();
			assertFalse(test.column("b").isPresent());
			count = db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);
			assertEquals(3, count);

			// test if trigger still exists
			assertTrue(test.triggers().findFirst().isPresent());
		}
	}

	@Test
	public void insertTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			InsertRow row = test.insert();
			row.set(1, "a", 3.14).execute();
			row.set(2, "b", 6.28).execute();

			assertEquals(2, (int)db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt));
			db.execute("SELECT COUNT(*) FROM test").mapFirst(Row::getInt);

			List<Double> res = db.execute("SELECT * FROM test ORDER BY a").map(r -> {
				assertEquals(Integer.class, r.getObject().getClass());
				assertEquals(String.class, r.getObject().getClass());
				assertEquals(Double.class, r.getObject().getClass());
				return r.getDouble(3);
			}).toList();

			assertEquals(Arrays.asList(3.14, 6.28), res);
		}
	}
}
