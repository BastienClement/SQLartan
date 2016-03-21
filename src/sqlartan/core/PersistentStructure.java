package sqlartan.core;

public abstract class PersistentStructure<T extends Column> implements Structure<T> {
	private Database database;
	private String name;

	protected PersistentStructure(Database database, String name) {
		this.database = database;
		this.name = name;
	}

	public String name() {
		return name;
	}

	public Database parentDatabase() {
		return database;
	}

	public abstract void rename(String newName);
	public abstract void duplicate(String newName);
	public abstract void drop();
}
