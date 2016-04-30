package sqlartan.view;

/**
 * Created by julien on 25.04.16.
 */
enum Type {DATABASE, TABLE, VIEW}

public class DbTreeItem {

	private String name;
	private Type type;

	public DbTreeItem(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}

	public String toString()
	{
		return name;
	}
}
