package sqlartan.core;

import java.util.TreeMap;

/**
 * Defines the different types
 */
public enum Affinity {
	/**
	 * All the types available in SQLITE
	 */
	Text(Type.Text),
	Numeric(Type.Real),
	Integer(Type.Integer),
	Real(Type.Real),
	Blob(Type.Blob);

	/**
	 * The type of the affinity
	 */
	public final Type type;

	/**
	 * Constructs a new affinity with the given type.
	 *
	 * @param type  the type of the affinity
	 */
	Affinity(Type type) {
		this.type = type;
	}

	/**
	 * Cache for the affinities
	 */
	private static TreeMap<String, Affinity> affinityCache = new TreeMap<>();

	/**
	 * Returns a new affinity with the specified type in String.
	 * Put it in the cache if it wasn't in it.
	 *
	 * @param type  the type of the affinity in a String
	 * @return the new affinity
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
	 * Transform a String to an affinity.
	 * Returns a numeric if the type wasn't found.
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
