package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;

public interface Node {
	default String toSQL() {
		Builder sql = new Builder();
		toSQL(sql);
		return sql.toString();
	}

	default void toSQL(Builder sql) {
		throw new UnsupportedOperationException(this.getClass().getCanonicalName());
	}
}
