package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.Tokenizable;

/**
 * SQL AST node
 */
public interface Node {
	/**
	 * Serialize this node back to a SQL string
	 */
	default String toSQL() {
		Builder sql = new Builder();
		toSQL(sql);
		return sql.toString();
	}

	/**
	 * Internal serialization method. Must be redefined.
	 * Default implementation default to throwing an exception.
	 *
	 * @param sql the SQL builder to use
	 */
	default void toSQL(Builder sql) {
		throw new UnsupportedOperationException(this.getClass().getCanonicalName());
	}

	/**
	 * Enumerated nodes
	 */
	interface Enumerated extends Node {}

	/**
	 * Common super-type for Keywords and Operators
	 */
	interface KeywordOrOperator extends Enumerated, Tokenizable<Token.Wrapper<? extends KeywordOrOperator>> {}
}
