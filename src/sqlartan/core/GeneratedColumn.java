package sqlartan.core;

public class GeneratedColumn extends Column {
	private boolean computed;
	private TableColumn sourceColumn;
	private String sourceExpr;

	GeneratedColumn(String name, String type, boolean notNull, TableColumn sourceColumn, String sourceExpr) {
		super(name, type, notNull);
		this.computed = sourceExpr != null;
		this.sourceColumn = sourceColumn;
		this.sourceExpr = sourceExpr;
	}

	public boolean isComputed() {
		return computed;
	}

	public TableColumn sourceColumn() {
		return sourceColumn;
	}

	public String sourceExpr() {
		return sourceExpr;
	}
}
