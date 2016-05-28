package sqlartan.core.util;

/**
 * Utility object to convert objects returned by the SQLite driver
 * to objects used as return type for Row accessors.
 * <p>
 * This object is the bridge between the classes used by the
 * SQLite driver for the ResultSet.getObject method and the
 * types returned by the accessor in the Row object.
 */
public abstract class DataConverter {
	/**
	 * Convert the given Object value to an instance of the requested class.
	 *
	 * @param value  the Object value from ResultSet.getObject
	 * @param tClass the target class to convert to
	 * @param <T>    the type of the result
	 */
	public static <T> T convert(Object value, Class<T> tClass) {
		// Find the converted to use for the requested class
		Converter<T> converter = converterForClass(tClass);

		// Ensure value is not null before doing more work
		if (value == null) return converter.fromNull();

		// Find the original type of the value
		if (value instanceof Integer) {
			return converter.fromInteger((Integer) value);
		} else if (value instanceof Long) {
			return converter.fromLong((Long) value);
		} else if (value instanceof Double) {
			return converter.fromDouble((Double) value);
		} else if (value instanceof String) {
			return converter.fromString((String) value);
		} else {
			// TODO: create additional converters if we missed a return type of the SQLite driver
			throw new UnsupportedOperationException("Cannot convert from " + value.getClass().getSimpleName());
		}
	}

	/**
	 * A converter constructing values of a specific class.
	 *
	 * @param <T> the type of the result of this Converter
	 */
	private interface Converter<T> {
		T fromNull();
		T fromInteger(Integer int_value);
		T fromLong(Long long_value);
		T fromDouble(Double double_value);
		T fromString(String str_value);
	}

	/**
	 * The data converter to Integer.
	 */
	private static class IntegerConverter implements Converter<Integer> {
		private static Integer Zero = 0;
		public Integer fromNull() { return Zero; }
		public Integer fromInteger(Integer int_value) { return int_value; }
		public Integer fromLong(Long long_value) { return long_value.intValue(); }
		public Integer fromDouble(Double double_value) { return double_value.intValue(); }
		public Integer fromString(String str_value) { return Integer.valueOf(str_value); }
	}

	/**
	 * The data converter to Long.
	 */
	private static class LongConverter implements Converter<Long> {
		private static Long zero = 0L;
		public Long fromNull() { return zero; }
		public Long fromInteger(Integer int_value) { return int_value.longValue(); }
		public Long fromLong(Long long_value) { return long_value; }
		public Long fromDouble(Double double_value) { return double_value.longValue(); }
		public Long fromString(String str_value) { return Long.valueOf(str_value); }
	}

	/**
	 * The data converter to Double.
	 */
	private static class DoubleConverter implements Converter<Double> {
		private static Double zero = 0.0;
		public Double fromNull() { return zero; }
		public Double fromInteger(Integer int_value) { return int_value.doubleValue(); }
		public Double fromLong(Long long_value) { return long_value.doubleValue(); }
		public Double fromDouble(Double double_value) { return double_value; }
		public Double fromString(String str_value) { return Double.valueOf(str_value); }
	}

	/**
	 * The data converter to String.
	 */
	private static class StringConverter implements Converter<String> {
		private static String empty = "";
		public String fromNull() { return empty; }
		public String fromInteger(Integer int_value) { return int_value.toString(); }
		public String fromLong(Long long_value) { return long_value.toString(); }
		public String fromDouble(Double double_value) { return double_value.toString(); }
		public String fromString(String str_value) { return str_value; }
	}

	private static IntegerConverter integerConverter = new IntegerConverter();
	private static LongConverter longConverter = new LongConverter();
	private static DoubleConverter doubleConverter = new DoubleConverter();
	private static StringConverter stringConverter = new StringConverter();

	/**
	 * Finds the converter matching the requested target class.
	 *
	 * @param tClass the target class
	 * @param <T>    the target type
	 */
	@SuppressWarnings("unchecked")
	private static <T> Converter<T> converterForClass(Class<T> tClass) {
		if (tClass == Integer.class) {
			return (Converter<T>) integerConverter;
		} else if (tClass == Long.class) {
			return (Converter<T>) longConverter;
		} else if (tClass == Double.class) {
			return (Converter<T>) doubleConverter;
		} else if (tClass == String.class) {
			return (Converter<T>) stringConverter;
		} else {
			// TODO: create additional methods if we want to convert to another class
			throw new UnsupportedOperationException("Cannot produce a converter to " + tClass.getSimpleName());
		}
	}
}
