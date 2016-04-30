package sqlartan.core.ast.token;


import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public class Operator extends Token {
	public static final int MAX_OPERATOR_LEN = 2;

	private static Map<String, Operator> operators = new HashMap<>();
	public final String symbol;

	private Operator(String symbol, int offset) {
		super(offset);
		this.symbol = symbol;
	}

	private Operator(String symbol) {
		this(symbol, -1);
		operators.put(symbol, this);
	}

	private static class OffsetOperator extends Operator {
		private OffsetOperator(String name, int offset) {
			super(name, offset);
		}
	}

	public static Operator from(String symbol, int offset) {
		Operator ref = operators.get(symbol.toUpperCase());
		return ref != null ? new OffsetOperator(ref.symbol, offset) : null;
	}

	protected String type() { return "Operator"; }
	protected String value() { return symbol; }

	public boolean equals(Object other) {
		return other instanceof Operator && ((Operator) other).symbol.equals(symbol);
	}

	public int hashCode() {
		return symbol.hashCode();
	}

	public static final Operator CONCAT = new Operator("||");

	public static final Operator MUL = new Operator("*");
	public static final Operator DIV = new Operator("/");
	public static final Operator MOD = new Operator("%");

	public static final Operator PLUS = new Operator("+");
	public static final Operator MINUS = new Operator("-");

	public static final Operator SHIFT_LEFT = new Operator("<<");
	public static final Operator SHIFT_RIGHT = new Operator(">>");
	public static final Operator BIT_AND = new Operator("&");
	public static final Operator BIT_OR = new Operator("|");

	public static final Operator LT = new Operator("<");
	public static final Operator LTE = new Operator("<=");
	public static final Operator GT = new Operator(">");
	public static final Operator GTE = new Operator(">=");

	public static final Operator EQ = new Operator("=");
	public static final Operator NOT_EQ = new Operator("<>");
	public static final Operator IS = new Operator("IS");
	public static final Operator IS_NOT = new Operator("IS NOT");
	public static final Operator IN = new Operator("IN");
	public static final Operator LIKE = new Operator("LIKE");
	public static final Operator GLOB = new Operator("GLOB");
	public static final Operator MATCH = new Operator("MATCH");
	public static final Operator REGEXP = new Operator("REGEXP");

	public static final Operator AND = new Operator("AND");
	public static final Operator OR = new Operator("OR");

	public static final Operator NOT = new Operator("NOT");

	public static final Operator DOT = new Operator(".");
	public static final Operator COMMA = new Operator(",");
	public static final Operator SEMICOLON = new Operator(";");
	public static final Operator LEFT_PAREN = new Operator("(");
	public static final Operator RIGHT_PAREN = new Operator(")");
}
