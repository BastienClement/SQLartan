package sqlartan.core;

import sqlartan.core.stream.IterableStream;
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
	public View duplicate(String newName) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void drop() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IterableStream<PersistentStructure<? extends Column>> sources() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IterableStream<GeneratedColumn> columns() {
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
