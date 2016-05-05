package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.Tokenizable;

public interface Node {
	default String toSQL() {
		Builder sql = new Builder();
		toSQL(sql);
		return sql.toString();
	}

	default void toSQL(Builder sql) {
		throw new UnsupportedOperationException(this.getClass().getCanonicalName());
	}

	/**
	 * Enumerated nodes
	 */
	interface Enumerated extends Node {}

	/**
	 * Common super-type for Keywords and Operators
	 * @param <S>
	 */
	interface KeywordOrOperator<S extends Token> extends Enumerated, Tokenizable<S> {}
}
