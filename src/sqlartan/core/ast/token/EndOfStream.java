package sqlartan.core.ast.token;

public class EndOfStream extends Token {
	private EndOfStream(String source, int offset) {
		super(source, offset);
	}

	public boolean equals(Object other) {
		return other instanceof EndOfStream;
	}

	public int hashCode() {
		return EndOfStream.class.hashCode();
	}

	protected String type() { return "EndOfStream"; }
	protected String value() { return ""; }

	public static EndOfStream at(String source, int offset) {
		return new EndOfStream(source, offset);
	}

	public static final EndOfStream EOS = new EndOfStream("", -1);
}
