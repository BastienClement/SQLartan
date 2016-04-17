package sqlartan.core;

import org.junit.Test;
import java.sql.SQLException;
import static org.junit.Assert.*;

public class DatabaseTests {
	@Test
	public void databaseLifecycleShouldBeCorrect() throws SQLException {
		Database db_ref;
		try (Database db = new Database()) {
			db_ref = db;
			assertEquals(db.path().getName(), ":memory:");
			assertEquals(db.name(), "main");
			assertEquals(db.isMemoryOnly(), true);
		}
		assertEquals(db_ref.isClosed(), true);
	}

	@Test
	public void sqliteVersionIsThreeEightEleven() throws SQLException {
		try (Database db = new Database()) {
			assertEquals("3.8.11", db.execute("SELECT sqlite_version()").mapFirst(Row::getString));
		}
	}

	@Test
	public void defaultPragmaAreCorrect() throws SQLException {
		try (Database db = new Database()) {
			int count_changes = db.execute("PRAGMA count_changes").mapFirst(Row::getInt);
			assertEquals(0, count_changes);
		}
	}
}
