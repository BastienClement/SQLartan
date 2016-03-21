package sqlartan.core;

import java.util.Optional;

public class ResultSet implements QueryStructure<GeneratedColumn> {
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
