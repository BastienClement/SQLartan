package sqlartan.core;

import java.util.LinkedList;

public class Index
{
	private String name;
	private boolean unique;
	private boolean primaryKey;
	private LinkedList<String> columns;

	public Index(String name, boolean unique, boolean primaryKey){
		this.name = name;
		this.unique = unique;
		this.primaryKey = primaryKey;
		columns = new LinkedList<String>();
	}

	public String getName(){
		return name;
	}

	public boolean isUnique(){
		return unique;
	}

	public boolean isPrimaryKey(){
		return primaryKey;
	}

	public void addColumn(String column){
		columns.add(column);
	}

	public LinkedList<String> getColumns(){
		return columns;
	}
}