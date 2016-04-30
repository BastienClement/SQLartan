package sqlartan.core.ast.token;


import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public class Operator implements Token {
	public static final int MAX_OPERATOR_LEN = 2;

	private static Map<String, Operator> operators = new HashMap<>();
	public final String symbol;

	private Operator(String symbol) {
		this.symbol = symbol;
		operators.put(symbol, this);
	}

	public String toString() {
		return "Operator(" + symbol + ")";
	}

	public static Operator from(String keyword) {
		return operators.get(keyword.toUpperCase());
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
