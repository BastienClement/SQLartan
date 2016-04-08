package sqlartan.core;

import java.util.List;
import java.util.Optional;

public class View extends PersistentStructure<GeneratedColumn> implements QueryStructure<GeneratedColumn> {
	protected View(Database database, String name) {
		super(database, name);
	}

	public void rename(String newName) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void duplicate(String newName) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void drop() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public List<PersistentStructure<? extends Column>> sources() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public List<GeneratedColumn> columns() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int columnCount() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Optional<GeneratedColumn> column(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Optional<GeneratedColumn> column(int idx) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
