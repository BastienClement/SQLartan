package sqlartan.core.ast.token;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL Operator
 */
@SuppressWarnings("StaticInitializerReferencesSubClass")
public class Operator extends Token<String> {
	/**
	 * The length of the lengthiest operator
	 * While parsing symbolic token, the lexer will always stops after parsing this number of characters.
	 */
	public static final int MAX_OPERATOR_LEN = 2;

	/**
	 * The list of every defined operator tokens
	 */
	private static Map<String, Operator> operators = new HashMap<>();

	/**
	 * Operator initialization
	 *
	 * @param symbol the operator symbol
	 * @param source the token source code
	 * @param offset the token offset in the source code
	 */
	private Operator(String symbol, String source, int offset) {
		super(TokenType.OPERATOR, source, offset, symbol);
	}

	/**
	 * Operator default initialization
	 * This method will register the operator in the operators map.
	 *
	 * @param symbol the operator symbol
	 */
	private Operator(String symbol) {
		this(symbol, "", -1);
		if (operators.containsKey(symbol)) {
			throw new IllegalStateException("An instance of " + symbol + " already exists");
		}
		operators.put(symbol, this);
	}

	/**
	 * Constructs an Operator token.
	 * If the requested operator does not exists, returns null.
	 *
	 * @param symbol the operator symbol
	 * @param source the source code
	 * @param offset the offset of the token in the source code
	 */
	public static Operator from(String symbol, String source, int offset) {
		Operator ref = operators.get(symbol.toUpperCase());
		return ref != null ? new Operator(ref.value, source, offset) : null;
	}

	/**
	 * Constructs a new instance of the same operator.
	 *
	 * @param source the operator source
	 * @param offset the offset of the token in the source code
	 */
	public Operator at(String source, int offset) {
		return new Operator(value, source, offset);
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
	public static final Operator DOT = new Operator(".");
	public static final Operator COMMA = new Operator(",");
	public static final Operator SEMICOLON = new Operator(";");
	public static final Operator LEFT_PAREN = new Operator("(");
	public static final Operator RIGHT_PAREN = new Operator(")");
	public static final Operator IS_NOT = new Operator("IS NOT");
}
