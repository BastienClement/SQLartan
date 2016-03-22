package sqlartan.core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Results implements QueryStructure<GeneratedColumn>, AutoCloseable {
	private ArrayList<GeneratedColumn> columns = new ArrayList<>();

	Results(Connection connection, String query) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			if (statement.execute(query)) {
				try (ResultSet rs = statement.getResultSet()) {
					ResultSetMetaData md = rs.getMetaData();
					int columns = md.getColumnCount();

					System.out.println("Columns: " + columns);
					for (int i = 1; i <= columns; i++) {
						System.out.println(String.format(
								"Column [%d] Label: %s; ColumnName: %s; Type: %d; TypeName: %s; SchemaName: %s; TableName: %s; Nullable: %s;",
								i,
								md.getColumnLabel(i),
								md.getColumnName(i),
								md.getColumnType(i),
								md.getColumnTypeName(i),
								md.getSchemaName(i),
								md.getTableName(i),
								md.isNullable(i)));
					}

					while (rs.next()) {
						System.out.print("< ");
						for (int i = 1; i <= columns; i++) {
							System.out.print("'" + rs.getString(i) + "' ");
						}
						System.out.print(">\n");
					}
				}
			} else {
				System.out.println("Updated: " + statement.getUpdateCount());
			}
		}
	}

	private void readMetadata(ResultSetMetaData meta) throws SQLException {
		int count = meta.getColumnCount();
		columns.ensureCapacity(count);

		for (int i = 1; i <= count; i++) {
			String name = meta.getColumnName(i);
			String table = meta.getTableName(i);

			GeneratedColumn col = new GeneratedColumn(new GeneratedColumn.Properties() {
				public boolean computed() { return table == null; }
				public TableColumn sourceColumn() { return null; }
				public String sourceExpr() { return null; }
				public String name() { return name; }

				@Override
				public String type() {
					return null;
				}

				@Override
				public boolean nullable() {
					return false;
				}
			});
		}
	}

	@Override
	public List<PersistentStructure<GeneratedColumn>> sources() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public List<GeneratedColumn> columns() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<GeneratedColumn> column(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<GeneratedColumn> column(int idx) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void close() throws Exception {

	}


}
