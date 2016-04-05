package sqlartan.core;

public abstract class Data {
	public abstract Type type();

	public int asInt() { throw new UnsupportedOperationException("Type is " + type()); }
	public long asLong() { throw new UnsupportedOperationException("Type is " + type()); }
	public float asFloat() { throw new UnsupportedOperationException("Type is " + type()); }
	public double asDouble() { throw new UnsupportedOperationException("Type is " + type()); }
	public String asString() { throw new UnsupportedOperationException("Type is " + type()); }
	public byte[] asByteArray() { throw new UnsupportedOperationException("Type is " + type()); }

	public static class Null extends Data {
		@Override
		public Type type() {
			return Type.Null;
		}
	}

	public static class Integer extends Data {
		private long value;
		public Integer(long value) { this.value = value; }
		public Type type() { return Type.Integer; }
		public int asInt() { return (int) value; }
		public long asLong() { return value; }
	}

	public static class Real extends Data {
		private double value;
		public Real(double value) { this.value = value; }
		public Type type() { return Type.Real; }
		public float asFloat() { return (float) value; }
		public double asDouble() { return value; }
	}

	public static class Text extends Data {
		private String value;
		public Text(String value) { this.value = value; }
		public Type type() { return Type.Text; }
		public String asString() { return value; }
	}

	public static class Blob extends Data {
		private byte[] value;
		public Blob(byte[] value) { this.value = value; }
		public Type type() { return Type.Blob; }
		public byte[] asByteArray() { return value; }
	}
}
