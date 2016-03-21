package sqlartan.core;

public abstract class Column {
	private String name;
	private String type;
	private Affinity affinity;
	private boolean notNull;

	protected Column(String name, String type, boolean notNull) {
		this.name = name;
		this.type = type;
		this.affinity = Affinity.forType(type);
		this.notNull = notNull;
	}

	public String name() {
		return name;
	}

	public String type() {
		return type;
	}

	public Affinity affinity() {
		return affinity;
	}

	public boolean isNotNull() {
		return notNull;
	}
}
