package sqlartan.core;

import org.junit.Test;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

public class TableTests {
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
}
