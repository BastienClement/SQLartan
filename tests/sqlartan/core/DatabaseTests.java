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
			Integer off = 0, on = 1;
			assertEquals(off, db.execute("PRAGMA count_changes").mapFirst(Row::getInt));
		}
	}

	//db.execute("CREATE TABLE main.foo (a text, b text, c integer);");
	//db.execute("CREATE TABLE main.bar (d intlol unique not null, e text check (length(e) > 2) primary key);");
	//db.execute("INSERT INTO bar VALUES(42);");
	//db.execute("PRAGMA table_info(bar)");
	//db.execute("SELECT * FROM sqlite_master");
	//db.execute("SELECT d FROM main.bar AS b");
	//db.execute("EXPLAIN QUERY PLAN SELECT d/2.0 FROM main.bar, foo AS b");
}
