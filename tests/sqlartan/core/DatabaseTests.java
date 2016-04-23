package sqlartan.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
}
