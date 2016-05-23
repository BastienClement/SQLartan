package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import java.util.Optional;

public interface ReadOnlyResult extends Structure<ResultColumn> {
	String query();
	boolean isQueryResult();
	boolean isUpdateResult();
	int updateCount();
	ImmutableList<ResultColumn> columns();
	Optional<ResultColumn> column(String name);
	Optional<ResultColumn> column(int idx);
}
