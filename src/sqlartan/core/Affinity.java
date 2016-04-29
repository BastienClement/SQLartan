package sqlartan.core;

import java.util.TreeMap;

/**
 * Defines the different types
 */
public enum Affinity {
	Text(Type.Text),
	Numeric(Type.Real),
	Integer(Type.Integer),
	Real(Type.Real),
	Blob(Type.Blob);

	public final Type type;

	Affinity(Type type) {
		this.type = type;
	}

	private static TreeMap<String, Affinity> affinityCache = new TreeMap<>();

	/**
	 *
	 * @param type
	 * @return
	 */
	public static Affinity forType(String type) {
		type = type.toUpperCase().trim();

		if (affinityCache.containsKey(type)) {
			return affinityCache.get(type);
		}

		Affinity a = parseType(type);
		affinityCache.put(type, a);
		return a;
	}

	/**
	 *
	 *
	 * @param type
	 * @return
	 */
	private static Affinity parseType(String type) {
		// https://www.sqlite.org/datatype3.html
		if (type.contains("INT")) {
			return Affinity.Integer;
		} else if (type.contains("CHAR") || type.contains("CLOB") || type.contains("TEXT")) {
			return Affinity.Text;
		} else if (type.contains("BLOB") || type.equals("")) {
			return Affinity.Blob;
		} else if (type.contains("REAL") || type.contains("FLOA") || type.contains("DOUB")) {
			return Affinity.Real;
		} else {
			return Affinity.Numeric;
		}
	}
}
