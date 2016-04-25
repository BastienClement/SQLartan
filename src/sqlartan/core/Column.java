package sqlartan.core;

public abstract class Column {
	interface Properties {
		String name();
		String type();
		boolean nullable();
	}

	private Properties props;

	protected Column(Properties props) {
		this.props = props;
	}

	public String name() {
		return props.name();
	}

	public String type() {
		return props.type();
	}

	public Affinity affinity() {
		return Affinity.forType(props.type());
	}

	public boolean nullable() {
		return props.nullable();
	}
}
