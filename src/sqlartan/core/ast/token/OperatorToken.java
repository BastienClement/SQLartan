package sqlartan.core.ast.token;


import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public class OperatorToken extends Token<String> {
	public static final int MAX_OPERATOR_LEN = 2;
	private static Map<String, OperatorToken> operators = new HashMap<>();

	private OperatorToken(String symbol, String source, int offset) {
		super(TokenType.OPERATOR, source, offset, symbol);
	}

	private OperatorToken(String symbol) {
		this(symbol, "", -1);
		if (operators.containsKey(symbol)) {
			throw new IllegalStateException("An instance of " + symbol + " already exists");
		}
		operators.put(symbol, this);
	}

	public static OperatorToken from(String symbol, String source, int offset) {
		OperatorToken ref = operators.get(symbol.toUpperCase());
		return ref != null ? new OperatorToken(ref.value, source, offset) : null;
	}

	public OperatorToken at(String source, int offset) {
		return new OperatorToken(value, source, offset);
	}

	public static final OperatorToken CONCAT = new OperatorToken("||");
	public static final OperatorToken MUL = new OperatorToken("*");
	public static final OperatorToken DIV = new OperatorToken("/");
	public static final OperatorToken MOD = new OperatorToken("%");
	public static final OperatorToken PLUS = new OperatorToken("+");
	public static final OperatorToken MINUS = new OperatorToken("-");
	public static final OperatorToken SHIFT_LEFT = new OperatorToken("<<");
	public static final OperatorToken SHIFT_RIGHT = new OperatorToken(">>");
	public static final OperatorToken BIT_AND = new OperatorToken("&");
	public static final OperatorToken BIT_OR = new OperatorToken("|");
	public static final OperatorToken LT = new OperatorToken("<");
	public static final OperatorToken LTE = new OperatorToken("<=");
	public static final OperatorToken GT = new OperatorToken(">");
	public static final OperatorToken GTE = new OperatorToken(">=");
	public static final OperatorToken EQ = new OperatorToken("=");
	public static final OperatorToken NOT_EQ = new OperatorToken("<>");
	public static final OperatorToken DOT = new OperatorToken(".");
	public static final OperatorToken COMMA = new OperatorToken(",");
	public static final OperatorToken SEMICOLON = new OperatorToken(";");
	public static final OperatorToken LEFT_PAREN = new OperatorToken("(");
	public static final OperatorToken RIGHT_PAREN = new OperatorToken(")");
	public static final OperatorToken IS_NOT = new OperatorToken("IS NOT");
}
