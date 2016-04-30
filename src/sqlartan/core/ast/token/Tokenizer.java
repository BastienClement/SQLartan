package sqlartan.core.ast.token;

import static sqlartan.core.ast.token.Tokenizer.NumericState.*;
import static sqlartan.core.ast.token.Tokenizer.State.*;

public class Tokenizer {
	enum State {
		WHITESPACE,
		NUM_PLACEHOLDER,
		ALPHA_PLACEHOLDER,
		STRING,
		IDENTIFIER,
		NUMERIC,
		ALPHA_FRAGMENT,
		SYM_FRAGMENT,
		SQL_COMMENT,
		C_COMMENT,
	}

	enum NumericState {
		INTEGER,
		DECIMAL,
		E,
		EXPONENT
	}

	private static String slice(char[] input, int start, int end) {
		return String.valueOf(input, start, end - start);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	public static TokenSource tokenize(String sql) {
		TokenSource.Builder builder = TokenSource.builder();

		char[] input = (sql + " ").toCharArray();
		int length = input.length;
		State state = WHITESPACE;

		int max_placeholder = 0;
		char quote_char = '\0';
		int begin = 0;

		outer:
		for (int i = 0; i < length; ++i) {
			char c = input[i];
			switch (state) {
				case WHITESPACE:
					if (!Character.isWhitespace(c)) {
						begin = i;
						if (c == '-' && input[i + 1] == '-') {
							state = SQL_COMMENT;
							++i;
						} else if (c == '/' && input[i + 1] == '*') {
							state = C_COMMENT;
							i += 2;
						} else if (c == '?') {
							state = NUM_PLACEHOLDER;
						} else if (c == ':') {
							state = ALPHA_PLACEHOLDER;
						} else if (c == '@') {
							state = ALPHA_PLACEHOLDER;
						} else if (c == '$') {
							// Tcl-style placeholders
							throw new UnsupportedOperationException();
						} else if (c == '\'') {
							state = STRING;
						} else if ((c == 'x' || c == 'X') && input[i + 1] == '\'') {
							throw new UnsupportedOperationException("BLOB literals are not supported");
						} else if (c == '[') {
							state = IDENTIFIER;
							quote_char = ']';
						} else if (c == '"' || c == '`') {
							state = IDENTIFIER;
							quote_char = c;
						} else if (Character.isDigit(c) || (c == '.' && Character.isDigit(input[i + 1])) ||
								(c == '-' && (input[i + 1] == '.' || Character.isDigit(input[i + 1])))) {
							state = NUMERIC;
							--i;
						} else if (Character.isLetter(c)) {
							state = ALPHA_FRAGMENT;
						} else {
							state = SYM_FRAGMENT;
						}
					}
					break;

				case NUM_PLACEHOLDER:
					if (!Character.isDigit(c)) {
						if (i - begin == 1) {
							++max_placeholder;
							builder.push(new Placeholder.Indexed(max_placeholder, begin));
						} else {
							int index = Integer.valueOf(slice(input, begin + 1, i));
							if (index > max_placeholder) max_placeholder = index;
							builder.push(new Placeholder.Indexed(index, begin));
						}
						state = State.WHITESPACE;
						--i;
					}
					break;

				case ALPHA_PLACEHOLDER:
					if (!Character.isLetter(c)) {
						if (i - begin == 1) {
							// Empty named placeholder
							throw new IllegalArgumentException();
						} else {
							String name = slice(input, begin + 1, i);
							builder.push(new Placeholder.Named(name, begin));
						}
						state = State.WHITESPACE;
						--i;
					}
					break;

				case STRING:
					if (c == '\'') {
						if (input[i + 1] == '\'') {
							// Single quote escape
							++i;
						} else {
							String value = slice(input, begin + 1, i).replace("''", "'");
							builder.push(Literal.from(value, begin));
							state = State.WHITESPACE;
						}
					}
					break;

				case IDENTIFIER:
					if (c == quote_char) {
						String fragment = slice(input, begin + 1, i);
						builder.push(Identifier.from(fragment, begin));
						state = State.WHITESPACE;
					}
					break;

				case NUMERIC:
					if (c == '0' && (input[i + 1] == 'x' || input[i + 1] == 'X')) {
						throw new UnsupportedOperationException("Hexadecimal integer literals are not supported");
					}

					boolean has_integer_part = false;
					boolean has_decimal_part = false;
					boolean valid_exponent = true;
					NumericState ns = INTEGER;

					if (c == '-') {
						++i;
					}

					int num_begin = i;
					scan:
					for (; i < length; ++i) {
						c = input[i];
						switch (ns) {
							case INTEGER:
								if (!Character.isDigit(c)) {
									if (c == '.') {
										ns = DECIMAL;
									} else if (c == 'e' || c == 'E') {
										ns = E;
									} else {
										break scan;
									}
								} else {
									has_integer_part = true;
								}
								break;

							case DECIMAL:
								if (!Character.isDigit(c)) {
									if (c == 'e' || c == 'E') {
										ns = E;
									} else {
										break scan;
									}
								} else {
									has_decimal_part = true;
								}
								break;

							case E:
								ns = EXPONENT;
								valid_exponent = false;
								if (c != '+' && c != '-') {
									--i;
								}
								break;

							case EXPONENT:
								if (!Character.isDigit(c)) {
									break scan;
								} else {
									valid_exponent = true;
								}
								break;
						}
					}

					String number = String.valueOf(input, begin, i - begin);

					if (num_begin == i || (!has_decimal_part && !has_integer_part) || !valid_exponent) {
						throw new IllegalArgumentException("Malformed number: " + number);
					}

					builder.push(Literal.from(number, begin));

					state = State.WHITESPACE;
					--i;
					break;

				case ALPHA_FRAGMENT:
					if (!Character.isLetter(c)) {
						String fragment = String.valueOf(input, begin, i - begin);

						Operator operator;
						Keyword keyword;

						switch (fragment) {
							case "==":
								operator = Operator.EQ;
								break;
							case "!=":
								operator = Operator.NOT_EQ;
								break;
							default:
								operator = Operator.from(fragment, begin);
								break;
						}

						if (operator != null) {
							if (operator == Operator.NOT && builder.last() == Operator.IS) {
								builder.pop();
								builder.push(Operator.IS_NOT);
							} else {
								builder.push(operator);
							}
						} else if ((keyword = Keyword.from(fragment, begin)) != null) {
							builder.push(keyword);
						} else {
							builder.push(Identifier.from(fragment, begin));
						}
						state = State.WHITESPACE;
						--i;
					}
					break;

				case SYM_FRAGMENT:
					if (Character.isLetter(c) || Character.isDigit(c) || Character.isWhitespace(c) || i - begin >= Operator.MAX_OPERATOR_LEN) {
						String fragment;
						do {
							fragment = String.valueOf(input, begin, i - begin);
							Operator operator = Operator.from(fragment, begin);
							if (operator != null) {
								builder.push(operator);
								state = State.WHITESPACE;
								--i;
								continue outer;
							}
						} while (--i > begin);
						throw new IllegalArgumentException("Illegal symbol encountered: '" + fragment + "'");
					}
					break;

				case SQL_COMMENT:
					if (c == '\n') {
						state = WHITESPACE;
					}
					break;

				case C_COMMENT:
					if (c == '/' && input[i - 1] == '*') {
						state = WHITESPACE;
					}
					break;
			}
		}

		if (state == STRING || state == IDENTIFIER) {
			throw new IllegalArgumentException("Unterminated string or identifier");
		}

		return builder.build();
	}
}
