package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.token.Token;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SQL language operators
 */
public enum Operator implements Node.KeywordOrOperator {
	CONCAT("||"),
	MUL("*"), DIV("/"), MOD("%"),
	PLUS("+"), MINUS("-"),
	SHIFT_LEFT("<<"), SHIFT_RIGHT(">>"), BIT_AND("&"), BIT_OR("|"),
	LT("<"), LTE("<="), GT(">"), GTE(">="),
	EQ("="), NOT_EQ("<>"),
	BIT_NOT("~"), DOT("."), COMMA(","), SEMICOLON(";"), LEFT_PAREN("("), RIGHT_PAREN(")");

	/**
	 * The operator symbol
	 */
	public final String symbol;

	/**
	 * A dummy token for this keyword
	 */
	public final Token.Operator token;

	Operator(String symbol) {
		this.symbol = symbol;
		this.token = Token.Operator.dummyFor(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toSQL(Builder sql) {
		sql.append(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Token.Operator token() { return token; }

	/**
	 * The length of the lengthiest operator
	 * While parsing symbolic token, the lexer will always stops after parsing this number of characters.
	 */
	public static final int maxLength;

	/**
	 * The list of every defined operators
	 */
	public static final Map<String, Operator> operators;

	/**
	 * Initialize static structures
	 */
	static {
		int max_length = 0;
		Map<String, Operator> ops = new HashMap<>();

		for (Operator op : values()) {
			String symbol = op.symbol;

			if (ops.containsKey(symbol)) {
				throw new IllegalStateException("An instance of " + symbol + " already exists");
			} else {
				ops.put(symbol, op);
			}

			if (symbol.length() > max_length) {
				max_length = symbol.length();
			}
		}

		maxLength = max_length;
		operators = Collections.unmodifiableMap(ops);
	}

	/**
	 * Returns the Operator matching the given symbol
	 */
	public static Optional<Operator> from(String symbol) {
		return Optional.ofNullable(operators.get(symbol));
	}
}
