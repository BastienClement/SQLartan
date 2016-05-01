package sqlartan.view;

/**
 * Created by julien on 25.04.16.
 */
enum Type {
	DATABASE, TABLE, VIEW
}

public class DbTreeItem {

	private String name;
	private Type type;

	public DbTreeItem(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * @return the type of the treeItem
	 */
	public Type type() {
		return type;
	}

	/**
	 * @return the name of the TreeItem
	 */
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
