package sqlartan.core;

import org.junit.Test;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;

public class DatabaseTests {
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
