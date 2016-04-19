package sqlartan.core;

public abstract class PersistentStructure<T extends Column> implements Structure<T> {
	protected final Database database;
	protected final String name;

	protected PersistentStructure(Database database, String name) {
		this.database = database;
		this.name = name;
	}

	public String name() {
		return name;
	}

	public Database database() {
		return database;
	}

	/**
	 * Returns the safe full name of this persistent structure for use in queries.
	 */
	public String fullName() {
		return "[" + database.name() + "].[" + name() + "]";
	}
	public abstract void rename(String newName);
	public abstract void duplicate(String newName);
	public abstract void drop();
}
