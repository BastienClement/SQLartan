package sqlartan.core;

import java.util.Optional;

public interface Structure<T extends Column> {
	T[] columns();
	Optional<T> column(String name);
	Optional<T> column(int idx);
}
