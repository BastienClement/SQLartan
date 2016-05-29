package sqlartan.core;

import java.util.TreeMap;

/**
 * SQLite affinities
 */
public enum Affinity {
	Text(Type.Text),
	Numeric(Type.Real),
	Integer(Type.Integer),
	Real(Type.Real),
	Blob(Type.Blob);

	/**
	 * The underlying type of the affinity
	 */
	public final Type type;

	/**
	 * @param type the type of the affinity
	 */
	Affinity(Type type) {
		this.type = type;
	}

	/**
	 * Cache of affinity for a each types
	 */
	private static TreeMap<String, Affinity> affinityCache = new TreeMap<>();

	/**
	 * Returns the affinity associated to the given type.
	 * <p>
	 * This method is memoized.
	 *
	 * @param type the column type
	 * @return the affinity associated with he given type
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
	 * Transforms a type name to an affinity.
	 *
	 * @param type the column type
	 * @return the affinity associated with the given type
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
