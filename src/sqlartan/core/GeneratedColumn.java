package sqlartan.core;

public class GeneratedColumn extends Column {
	public static interface Properties extends Column.Properties {
		boolean computed();
		TableColumn sourceColumn();
		String sourceExpr();
	}

	private Properties props;

	GeneratedColumn(Properties props) {
		super(props);
		this.props = props;
	}

	public boolean isComputed() {
		return props.computed();
	}

	public TableColumn sourceColumn() {
		return props.sourceColumn();
	}

	public String sourceExpr() {
		return props.sourceExpr();
	}
}
