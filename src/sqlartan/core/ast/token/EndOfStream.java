package sqlartan.core.ast.token;

public class EndOfStream extends Token {
	private EndOfStream(int offset) {
		super(offset);
	}

	public boolean equals(Object other) {
		return other instanceof EndOfStream;
	}

	public int hashCode() {
		return EndOfStream.class.hashCode();
	}

	protected String type() { return "EndOfStream"; }
	protected String value() { return ""; }

	public static EndOfStream at(int offset) {
		return new EndOfStream(offset);
	}

	public static final EndOfStream EOS = new EndOfStream(-1);
}
