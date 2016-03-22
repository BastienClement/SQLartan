package sqlartan.core;

import java.util.List;

public interface QueryStructure<T extends GeneratedColumn> extends Structure<T> {
	List<PersistentStructure<GeneratedColumn>> sources();
}
