package sqlartan.core;

import org.junit.Test;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

	//db.execute("CREATE TABLE main.foo (a text, b text, c integer);");
	//db.execute("CREATE TABLE main.bar (d intlol unique not null, e text check (length(e) > 2) primary key);");
	//db.execute("INSERT INTO bar VALUES(42);");
	//db.execute("PRAGMA table_info(bar)");
	//db.execute("SELECT * FROM sqlite_master");
	//db.execute("SELECT d FROM main.bar AS b");
	//db.execute("EXPLAIN QUERY PLAN SELECT d/2.0 FROM main.bar, foo AS b");
}
