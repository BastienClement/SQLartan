package sqlartan.core;

import java.util.Optional;

public class View extends PersistentStructure<GeneratedColumn> implements QueryStructure<GeneratedColumn> {
	protected View(Database database, String name) {
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
	public PersistentStructure<GeneratedColumn>[] sources() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public GeneratedColumn[] columns() {
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
}
