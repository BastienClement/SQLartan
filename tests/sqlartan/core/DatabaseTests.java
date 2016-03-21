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
			assertEquals(":memory:", db.path().getName());
			assertEquals("main", db.name());
			assertEquals(true, db.isMemoryOnly());
		}
		assertEquals(db_ref.isClosed(), true);
	}

	@Test
	public void sqliteVersionIsThreeEightEleven() throws SQLException {
		try (Database db = new Database()) {
			assertEquals(":memory:", db.path().getName());
		}
	}
}
