package sqlartan.core;

import java.util.Optional;

public class TableColumn extends Column {
	private Table table;
	private boolean unique;
	private String check;

	TableColumn(Table table, String name, String type, boolean notNull, boolean unique, String check) {
		super(name, type, notNull);
		this.table = table;
		this.unique = unique;
		this.check = check;
	}

	public Table parentTable() {
		return table;
	}

	public boolean unique() {
		return unique;
	}

	public Optional<String> check() {
		return Optional.ofNullable(check);
	}
}
