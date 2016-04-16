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

	@Test
	public void tablesColumnsTests() throws SQLException {
		try (Database db = new Database()) {
			db.execute("CREATE TABLE test (a INT PRIMARY KEY, b TEXT UNIQUE, c FLOAT)");
			Table test = db.table("test").get();

			List<TableColumn> columns = test.columns().toList();
			assertEquals(3, columns.size());

			List<String> names = Arrays.asList("a", "b", "c");
			assertEquals(names, columns.stream().map(Column::name).collect(Collectors.toList()));

			List<String> types = Arrays.asList("INT", "TEXT", "FLOAT");
			assertEquals(types, columns.stream().map(Column::type).collect(Collectors.toList()));

			List<Affinity> affinities = Arrays.asList(Affinity.Integer, Affinity.Text, Affinity.Real);
			assertEquals(affinities, columns.stream().map(Column::affinity).collect(Collectors.toList()));
		}
	}
}
