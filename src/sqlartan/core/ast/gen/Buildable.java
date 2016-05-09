package sqlartan.core.ast.gen;

@FunctionalInterface
public interface Buildable {
	void toSQL(Builder sql);
}
