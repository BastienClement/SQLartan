package sqlartan.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import sqlartan.core.ast.token.TokenizeException;
import sqlartan.core.stream.ImmutableList;
import sqlartan.core.util.UncheckedSQLException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import static org.junit.Assert.*;

public class DatabaseTests {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void databaseLifecycleShouldBeCorrect() throws SQLException {
		Database db_ref;
		try (Database db = Database.createEphemeral()) {
			db_ref = db;
			assertEquals(":memory:", db.path().getName());
			assertEquals("main", db.name());
			assertTrue(db.isEphemeral());
		}
		assertTrue(db_ref.isClosed());
	}

	@Test
	public void databaseOpenFileTests() throws IOException, SQLException {
		// Opening a blank file should work
		File goodFile = folder.newFile();
		try (Database db = Database.open(goodFile)) {
			db.execute("CREATE TABLE foo (a INT, b TEXT)");
			db.execute("INSERT INTO foo VALUES (2, 'b')");
		}

		// Reading a DB file should work too, obviously
		try (Database db = Database.open(goodFile)) {
			int a = db.execute("SELECT a FROM foo").mapFirst(Row::getInt);
			assertEquals(2, a);
		}

		// Creating a file that is neither blank nor a valid database
		File badFile = folder.newFile();
		FileWriter writer = new FileWriter(badFile);
		writer.write("I'm not a database file");
		writer.flush();
		writer.close();

		// Opening it should throw an exception
		exception.expect(SQLException.class);
		Database.open(badFile);
	}

	@Test
	public void attachTests() throws IOException, SQLException {
		File mainFile = folder.newFile();
		File attachedFile = folder.newFile();

		for (File file : Arrays.asList(mainFile, attachedFile)) {
			try (Database db = Database.open(file)) {
				db.assemble("CREATE TABLE ", file.getName() ," (a INT, b TEXT)").execute();
			}
		}

		try (Database main = Database.open(mainFile);
		     AttachedDatabase attached = main.attach(attachedFile, "attached")) {
			assertEquals(Collections.singletonList(mainFile.getName()), main.tables().map(Table::name).toList());
			assertEquals(Collections.singletonList(attachedFile.getName()), attached.tables().map(Table::name).toList());

			assertSame(attached, main.attached().get("attached"));
			Set<String> keys = main.attached().keySet();
			assertEquals(1, keys.size());
			assertTrue(keys.contains("attached"));

			//noinspection OptionalGetWithoutIsPresent
			assertSame(attached, main.attached("attached").get());
			assertFalse(main.attached("foo").isPresent());

			attached.close();
			assertEquals(0, main.attached().size());

			exception.expect(UncheckedSQLException.class);
			attached.tables();
		}
	}

	@Test
	public void sqliteVersionIsThreeEightEleven() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			assertEquals("3.8.11", db.execute("SELECT sqlite_version()").mapFirst(Row::getString));
		}
	}

	@Test
	public void defaultPragmaAreCorrect() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			int count_changes = db.execute("PRAGMA count_changes").mapFirst(Row::getInt);
			assertEquals(0, count_changes);
		}
	}

	@Test
	public void structureListTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE a (z INT)");
			db.execute("CREATE TABLE c (z INT)");
			db.execute("CREATE TABLE b (z INT)");
			db.execute("CREATE VIEW y AS SELECT * FROM b");
			db.execute("CREATE VIEW x AS SELECT * FROM a");

			List<String> tnames = db.tables().map(Table::name).toList();
			assertEquals(Arrays.asList("a", "b", "c"), tnames);

			Optional<Table> ta = db.table("a");
			Optional<Table> tx = db.table("x");

			assertTrue(ta.isPresent());
			assertFalse(tx.isPresent());

			List<String> vnames = db.views().map(View::name).toList();
			assertEquals(Arrays.asList("x", "y"), vnames);

			Optional<View> vx = db.view("x");
			Optional<View> va = db.view("a");

			assertTrue(vx.isPresent());
			assertFalse(va.isPresent());
		}
	}

	@Test
	public void exportCanBeImported() throws SQLException{
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE foo (\n" +
					"    id INTEGER NOT NULL PRIMARY KEY\n" +
					"  );");
			db.execute("CREATE TABLE bar (" +
					"    id INTEGER NOT NULL PRIMARY KEY,\n" +
					"    foo_id INTEGER NOT NULL\n" +
					"           CONSTRAINT fk_foo_id REFERENCES foo(id) ON DELETE CASCADE,\n" +
					"    foo_str TEXT\n" +
				"  );");
			db.execute("INSERT INTO foo VALUES (1), (2), (3), (4)");
			db.execute("INSERT INTO bar VALUES (1, 1, 'abc'), (2, 1, 'ub'), (3, 2, NULL), (4, 3, 'Madafak')");
			db.execute("CREATE VIEW foo_bar AS\n" +
					"  SELECT foo.id AS fooid, bar.id AS barid\n" +
					"  FROM foo, bar\n" +
					"  WHERE 0=0;");
			db.execute("CREATE TRIGGER fki_bar_foo_id\n" +
				"  BEFORE INSERT ON bar\n" +
				"  FOR EACH ROW BEGIN\n" +
				"      SELECT RAISE(ROLLBACK, 'insert on table \"bar\" violates foreign key constraint \"fk_foo_id\"')\n" +
				"      WHERE  (SELECT id FROM foo WHERE id = NEW.foo_id) IS NULL;\n" +
				"  END;");
			db.execute("CREATE TRIGGER fki_bar_foo2_id\n" +
				"  BEFORE INSERT ON bar\n" +
				"  FOR EACH ROW BEGIN\n" +
				"      SELECT RAISE(ROLLBACK, 'insert on table \"bar\" violates foreign key constraint \"fk_foo_id\"')\n" +
				"      WHERE  (SELECT id FROM foo WHERE id = NEW.foo_id) IS NULL;\n" +
				"  END;");

			assertEquals(db.export(), "PRAGMA foreign_keys=OFF;\n" +
				"BEGIN TRANSACTION;\n" +
				"CREATE TABLE foo (\n" +
				"    id INTEGER NOT NULL PRIMARY KEY\n" +
				"  );\n" +
				"CREATE TABLE bar (    id INTEGER NOT NULL PRIMARY KEY,\n" +
				"    foo_id INTEGER NOT NULL\n" +
				"           CONSTRAINT fk_foo_id REFERENCES foo(id) ON DELETE CASCADE,\n" +
				"    foo_str TEXT\n" +
				"  );\n" +
				"INSERT INTO [main].[bar] VALUES (1, 1, 'abc'), (2, 1, 'ub'), (3, 2, NULL), (4, 3, 'Madafak');\n" +
				"INSERT INTO [main].[foo] VALUES (1), (2), (3), (4);\n" +
				"CREATE VIEW foo_bar AS\n" +
				"  SELECT foo.id AS fooid, bar.id AS barid\n" +
				"  FROM foo, bar\n" +
				"  WHERE 0=0;\n" +
				"CREATE TRIGGER fki_bar_foo_id\n" +
				"  BEFORE INSERT ON bar\n" +
				"  FOR EACH ROW BEGIN\n" +
				"      SELECT RAISE(ROLLBACK, 'insert on table \"bar\" violates foreign key constraint \"fk_foo_id\"')\n" +
				"      WHERE  (SELECT id FROM foo WHERE id = NEW.foo_id) IS NULL;\n" +
				"  END;\n" +
				"CREATE TRIGGER fki_bar_foo2_id\n" +
				"  BEFORE INSERT ON bar\n" +
				"  FOR EACH ROW BEGIN\n" +
				"      SELECT RAISE(ROLLBACK, 'insert on table \"bar\" violates foreign key constraint \"fk_foo_id\"')\n" +
				"      WHERE  (SELECT id FROM foo WHERE id = NEW.foo_id) IS NULL;\n" +
				"  END;\n" +
				"COMMIT;");
		}
	}

	@Test
	public void importShouldExecuteSQLOnDatabase() throws SQLException, TokenizeException {
		try (Database db = Database.createEphemeral()) {
			db.importFromString("PRAGMA foreign_keys=OFF;\n" +
				"BEGIN TRANSACTION;\n" +
				"CREATE TABLE foo (\n" +
				"    id INTEGER NOT NULL PRIMARY KEY\n" +
				"  );\n" +
				"CREATE TABLE bar (    id INTEGER NOT NULL PRIMARY KEY,\n" +
				"    foo_id INTEGER NOT NULL\n" +
				"           CONSTRAINT fk_foo_id REFERENCES foo(id) ON DELETE CASCADE,\n" +
				"    foo_str TEXT\n" +
				"  );\n" +
				"INSERT INTO [main].[bar] VALUES (1, 1, 'abc'), (2, 1, 'ub'), (3, 2, NULL), (4, 3, 'Madafak');\n" +
				"INSERT INTO [main].[foo] VALUES (1), (2), (3), (4);\n" +
				"CREATE VIEW foo_bar AS\n" +
				"  SELECT foo.id AS fooid, bar.id AS barid\n" +
				"  FROM foo, bar\n" +
				"  WHERE 0=0;\n" +
				"CREATE TRIGGER fki_bar_foo_id\n" +
				"  BEFORE INSERT ON bar\n" +
				"  FOR EACH ROW BEGIN\n" +
				"      SELECT RAISE(ROLLBACK, 'insert on table \"bar\" violates foreign key constraint \"fk_foo_id\"')\n" +
				"      WHERE  (SELECT id FROM foo WHERE id = NEW.foo_id) IS NULL;\n" +
				"  END;\n" +
				"CREATE TRIGGER fki_bar_foo2_id\n" +
				"  BEFORE INSERT ON bar\n" +
				"  FOR EACH ROW BEGIN\n" +
				"      SELECT RAISE(ROLLBACK, 'insert on table \"bar\" violates foreign key constraint \"fk_foo_id\"')\n" +
				"      WHERE  (SELECT id FROM foo WHERE id = NEW.foo_id) IS NULL;\n" +
				"  END;\n" +
					"COMMIT;");

			assertEquals(4, db.assemble("SELECT COUNT(*) FROM foo").execute().mapFirst(Row::getInt).intValue());
		}
	}

	@Test
	public void executeMultiTest() throws SQLException, TokenizeException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE foo (bar TEXT)");
			db.execute("INSERT INTO foo VALUES ('a'), ('b'), ('c')");

			String query =
				"SELECT bar FROM foo WHERE bar = 'a';" +
				"SELECT bar FROM foo WHERE bar = 'b';" +
				"SELECT bar FROM foo WHERE bar = 'c';";

			List<Result> results = new ArrayList<>();

			ImmutableList<String> data = db.executeMulti(query)
			                               .peek(results::add)
			                               .map(res -> res.mapFirst(Row::getString))
			                               .toList();

			assertEquals(Arrays.asList("a", "b", "c"), data);
			assertEquals(true, results.stream().map(Result::isClosed).allMatch(closed -> closed));
		}
	}
}
