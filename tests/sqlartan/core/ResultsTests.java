package sqlartan.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sqlartan.core.stream.ImmutableList;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import static org.junit.Assert.*;

public class ResultsTests {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	@SuppressWarnings("StatementWithEmptyBody")
	public void resultShouldCloseWhenFullyConsumed() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			// Test iterator interface
			Result r1 = db.execute("SELECT sqlite_version()");
			for (Row row : r1) {}
			assertTrue(r1.isClosed());

			// Test stream interface
			Result r2 = db.execute("SELECT sqlite_version()");
			r2.forEach(row -> {});
			assertTrue(r2.isClosed());

			// Create test table with dummy data
			db.execute("CREATE TABLE foo ( bar INT, baz TEXT)");
			db.execute("INSERT INTO foo VALUES (1, 'a'), (2, 'b'), (3, 'c')");

			// We have more than one row in the table
			int count = db.execute("SELECT COUNT(*) FROM foo").mapFirst(Row::getInt);
			assertTrue(count > 1);

			// Limit the stream
			Result r3 = db.execute("SELECT * FROM foo");
			r3.limit(1).forEach(row -> {});
			/*assertFalse(r3.isClosed());

			// Manual close
			r3.close();*/
			assertTrue(r3.isClosed());

			// Mapping the first row
			Result r4 = db.execute("SELECT * FROM foo");
			r4.mapFirst(row -> row);
			assertTrue(r4.isClosed());
		}
	}

	@Test
	public void resultShouldHaveCorrectType() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			BiConsumer<String, Boolean> test = (sql, query) -> {
				try (Result res = db.execute(sql)) {
					assertEquals(query, res.isQueryResult());
					assertEquals(!query, res.isUpdateResult());
				} catch (SQLException e) {
					fail(e.getMessage());
				}
			};

			test.accept("CREATE TABLE foo (bar TEXT)", false);
			test.accept("UPDATE foo SET bar = 2", false);
			test.accept("DELETE FROM foo", false);

			test.accept("SELECT 2", true);
			test.accept("PRAGMA table_info(foo)", true);
		}
	}

	@Test
	public void updateResultShouldAlreadyBeClosed() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			Result r = db.execute("CREATE TABLE foo (a INT)");
			assertTrue(r.isClosed());
		}
	}

	@Test
	public void resultIterationIsOrdered() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE foo (bar INT)");
			db.execute("INSERT INTO foo VALUES (1),(2),(5),(3),(4),(8),(7),(9),(6)");

			Result res = db.execute("SELECT * FROM foo ORDER BY bar ASC");
			int max = res.mapToInt(Row::getInt)
			             .reduce(0, (prev, cur) -> {
				             if (prev > cur) throw new RuntimeException("Bad iteration order");
				             return cur;
			             });

			assertEquals(9, max);
			assertTrue(res.isClosed());
		}
	}

	@Test
	public void updateCountIsCorrect() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE foo (bar INT, baz TEXT)");

			assertEquals(3, db.execute("INSERT INTO foo VALUES (1, 'a'), (2, 'b'), (3, 'c')").updateCount());
			assertEquals(2, db.execute("UPDATE foo SET baz = 'z' WHERE bar < 3").updateCount());
			assertEquals(2, db.execute("DELETE FROM foo WHERE baz = 'z'").updateCount());
			assertEquals(1, db.execute("DELETE FROM foo").updateCount());
		}
	}

	@Test
	public void storedResultCanBeIteratedMultipleTimes() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE foo (bar INT)");
			db.execute("INSERT INTO foo VALUES (1),(2),(3),(4),(5)");

			ImmutableList<Row> res = db.execute("SELECT * FROM foo ORDER BY bar ASC").toList();

			List<Integer> l1 = res.map(Row::getInt);
			List<Integer> l2 = res.map(Row::getInt);

			assertEquals(l1, l2);
		}
	}

	@Test
	public void nonStoredResultCannotBeIteratedMultipleTimes() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			Result res = db.execute("SELECT 1, 2, 3");
			res.forEach(row -> {});

			exception.expect(IllegalStateException.class);
			res.forEach(row -> {});
		}
	}

	@Test
	public void updateTests() throws SQLException {
		try (Database db = Database.createEphemeral()) {
			db.execute("CREATE TABLE foo (a INT PRIMARY KEY, b INT UNIQUE, c TEXT)");
			db.execute("INSERT INTO foo VALUES (1, 2, 'a'), (2, 3, 'b'), (3, 4, 'c')");

			try (Result res = db.execute("SELECT * FROM foo")) {
				Row row = res.findFirst().get();
				boolean editable = row.editable();
				assertTrue(editable);
				row.update(1, 42);
			}

			try (Result res = db.execute("SELECT a, c FROM foo")) {
				Row row = res.findFirst().get();
				boolean editable = row.editable();
				assertTrue(editable);
				row.update(1, 42);
			}

			try (Result res = db.execute("SELECT b, c FROM foo")) {
				Row row = res.findFirst().get();
				boolean editable = row.editable();
				assertTrue(editable);
				row.update(1, 42);
			}

			try (Result res = db.execute("SELECT c FROM foo")) {
				Row row = res.findFirst().get();
				boolean editable = row.editable();
				assertFalse(editable);
				exception.expect(UnsupportedOperationException.class);
				row.update(0, 42);
			}
		}
	}
}
