package sqlartan.core.util;

public abstract class DataConverter {
	public static <T> T convert(Object value, Class<T> tClass) {
		Converter<T> converter = converterForClass(tClass);

		if (value == null) return converter.fromNull();
		Class<?> vClass = value.getClass();

		if (vClass == Integer.class) {
			return converter.fromInteger((Integer) value);
		} else if (vClass == Long.class) {
			return converter.fromLong((Long) value);
		} else if (vClass == Double.class) {
			return converter.fromDouble((Double) value);
		} else if (vClass == String.class) {
			return converter.fromString((String) value);
		} else {
			throw new UnsupportedOperationException("Cannot convert from " + vClass.getSimpleName());
		}
	}

	private interface Converter<T> {
		T fromNull();
		T fromInteger(Integer int_value);
		T fromLong(Long long_value);
		T fromDouble(Double double_value);
		T fromString(String str_value);
	}

	private static class IntegerConverter implements Converter<Integer> {
		private static Integer Zero = 0;
		public Integer fromNull() { return Zero; }
		public Integer fromInteger(Integer int_value) { return int_value; }
		public Integer fromLong(Long long_value) { return long_value.intValue(); }
		public Integer fromDouble(Double double_value) { return double_value.intValue(); }
		public Integer fromString(String str_value) { return Integer.valueOf(str_value); }
	}

	private static class LongConverter implements Converter<Long> {
		private static Long Zero = 0L;
		public Long fromNull() { return Zero; }
		public Long fromInteger(Integer int_value) { return int_value.longValue(); }
		public Long fromLong(Long long_value) { return long_value; }
		public Long fromDouble(Double double_value) { return double_value.longValue(); }
		public Long fromString(String str_value) { return Long.valueOf(str_value); }
	}

	private static class DoubleConverter implements Converter<Double> {
		private static Double Zero = 0.0;
		public Double fromNull() { return Zero; }
		public Double fromInteger(Integer int_value) { return int_value.doubleValue(); }
		public Double fromLong(Long long_value) { return long_value.doubleValue(); }
		public Double fromDouble(Double double_value) { return double_value; }
		public Double fromString(String str_value) { return Double.valueOf(str_value); }
	}

	private static class StringConverter implements Converter<String> {
		private static String Empty = "";
		public String fromNull() { return Empty; }
		public String fromInteger(Integer int_value) { return int_value.toString(); }
		public String fromLong(Long long_value) { return long_value.toString(); }
		public String fromDouble(Double double_value) { return double_value.toString(); }
		public String fromString(String str_value) { return str_value; }
	}

	private static IntegerConverter integerConverter = new IntegerConverter();
	private static LongConverter longConverter = new LongConverter();
	private static DoubleConverter doubleConverter = new DoubleConverter();
	private static StringConverter stringConverter = new StringConverter();

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
			throw new UnsupportedOperationException("Cannot produce a converter to " + tClass.getSimpleName());
		}
	}
}
