package sqlartan.core;

public interface QueryStructure<T extends GeneratedColumn> extends Structure<T> {
	PersistentStructure<T>[] sources();
}
