package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import java.util.Optional;

public interface ReadOnlyResult extends Structure<GeneratedColumn> {
	String query();
	boolean isQueryResult();
	boolean isUpdateResult();
	int updateCount();
	ImmutableList<GeneratedColumn> columns();
	Optional<GeneratedColumn> column(String name);
	Optional<GeneratedColumn> column(int idx);
}
