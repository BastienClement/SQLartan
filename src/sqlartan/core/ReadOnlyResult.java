package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.stream.IterableStream;
import java.util.Optional;

public interface ReadOnlyResult extends QueryStructure<GeneratedColumn> {
	String query();
	boolean isQueryResult();
	boolean isUpdateResult();
	int updateCount();
	IterableStream<PersistentStructure<? extends Column>> sources();
	ImmutableList<GeneratedColumn> columns();
	Optional<GeneratedColumn> column(String name);
	Optional<GeneratedColumn> column(int idx);
}
