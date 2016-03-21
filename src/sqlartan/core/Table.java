package sqlartan.core;

import java.util.Optional;

public class Table extends PersistentStructure<TableColumn> {
	Table(Database database, String name) {
		super(database, name);
	}

	@Override
	public void rename(String newName) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void duplicate(String newName) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void drop() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public TableColumn[] columns() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<TableColumn> column(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<TableColumn> column(int idx) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
