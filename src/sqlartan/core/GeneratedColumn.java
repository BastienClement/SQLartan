package sqlartan.core;

public class GeneratedColumn extends Column {
	interface Properties extends Column.Properties {
		String sourceTable();
		String sourceExpr();
	}

	private Properties props;

	GeneratedColumn(Properties props) {
		super(props);
		this.props = props;
	}

	public boolean isComputed() {
		return props.sourceTable() == null;
	}

	public TableColumn sourceColumn() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String sourceExpr() {
		return props.sourceExpr();
	}
}
