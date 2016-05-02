package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;

public interface Node {
	default String toSQL() {
		SQLBuilder sql = new SQLBuilder();
		toSQL(sql);
		return sql.toString();
	}

	default void toSQL(SQLBuilder sql) {
		throw new UnsupportedOperationException();
	}
}
