package sqlartan.core;

import sqlartan.core.util.DataConverter;

/**
 * Types of data supported by SQLite
 */
public enum Type {
	Null(Object.class),
	Integer(java.lang.Integer.class),
	Real(Double.class),
	Text(String.class),
	Blob(Object.class);

	private Class<?> javaType;

	Type(Class<?> java) {
		this.javaType = java;
	}

	public Object convert(Object from) {
		return DataConverter.convert(from, javaType);
	}
}
