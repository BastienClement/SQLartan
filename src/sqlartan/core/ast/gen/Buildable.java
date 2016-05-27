package sqlartan.core.ast.gen;

/**
 * An interface describing an object that can be built to an SQL fragment.
 */
@FunctionalInterface
public interface Buildable {
	/**
	 * Builds an SQL fragment representing this object.
	 *
	 * @param sql the Builder to use for building the fragment
	 */
	void toSQL(Builder sql);
}
