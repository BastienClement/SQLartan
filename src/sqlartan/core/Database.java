package sqlartan.core;

public class Database {
	private String name;
	private String path;

	public Database() {
		this(":memory:");
	}

	public Database(String path) {
		this(path, "main");
	}

	protected Database(String path, String name) {
		this.name = name;
		this.path = path;
	}

	public String name() {
		return name;
	}

	public String path() {
		return path;
	}

	public void close() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public class AttachedDatabase extends Database {
		public Database mainDatabase() {
			return Database.this;
		}
	}
}
