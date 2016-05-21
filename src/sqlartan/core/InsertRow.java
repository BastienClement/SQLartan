package sqlartan.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class InsertRow {
	private Table table;
	private List<Object> data = new ArrayList<>();

	InsertRow(Table table) {
		this.table = table;
	}

	public InsertRow set(Object value) {
		data.add(value);
		return this;
	}

	public InsertRow set(int index, Object value) {
		while (data.size() <= index) {
			data.add(null);
		}
		data.add(index, value);
		return this;
	}

	public InsertRow set(Object... values) {
		for (Object value : values) set(value);
		return this;
	}

	public Result execute() throws SQLException {
		int cardinality = data.size();

		String phs = String.join(", ", (Iterable<String>) Stream.generate(() -> "?").limit(cardinality)::iterator);
		PreparedQuery query = table.database.assemble("INSERT INTO ", table.fullName(), " VALUES (" + phs + ")")
		                                    .prepare();

		for (int i = 0; i < cardinality; i++) {
			query.set(i + 1, data.get(i));
		}

		data.clear();
		return query.execute();
	}
}
