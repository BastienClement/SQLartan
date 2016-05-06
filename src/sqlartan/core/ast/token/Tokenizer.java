package sqlartan.core.ast.token;

import sqlartan.core.ast.Keyword;
import sqlartan.core.ast.Operator;
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

	@SuppressWarnings({ "StatementWithEmptyBody", "ConstantConditions" })
	public static TokenSource tokenize(String sql) throws TokenizeException {
		TokenSource.Builder builder = TokenSource.builder();

		char[] input = (sql + " ").toCharArray();
		int length = input.length;
		State state = WHITESPACE;

		int highest_placeholder = 0;
		char quote_char = '\0';
		int token_start = 0;

		outer:
		for (int i = 0; i < length; ++i) {
			char c = input[i];
			switch (state) {
				case WHITESPACE:
					if (!Character.isWhitespace(c)) {
						token_start = i;
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
							throw new TokenizeException("Tcl-style placeholders are not supported", sql, token_start);
						} else if (c == '\'') {
							state = STRING;
						} else if ((c == 'x' || c == 'X') && input[i + 1] == '\'') {
							throw new TokenizeException("BLOB literals are not supported", sql, token_start);
						} else if (c == '[') {
							state = IDENTIFIER;
							quote_char = ']';
						} else if (c == '"' || c == '`') {
							state = IDENTIFIER;
							quote_char = c;
						} else if (Character.isDigit(c) || (c == '.' && Character.isDigit(input[i + 1]))) {
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
						if (i - token_start == 1) {
							++highest_placeholder;
							builder.push(Token.Placeholder.fromIndex(highest_placeholder, sql, token_start));
						} else {
							int index = Integer.valueOf(slice(input, token_start + 1, i));
							if (index > highest_placeholder) highest_placeholder = index;
							builder.push(Token.Placeholder.fromIndex(index, sql, token_start));
						}
						state = WHITESPACE;
						--i;
					}
					break;

				case ALPHA_PLACEHOLDER:
					if (!Character.isLetter(c)) {
						if (i - token_start == 1) {
							// Empty named placeholder
							throw new TokenizeException("Empty named placeholder", sql, token_start);
						} else {
							String name = slice(input, token_start + 1, i);
							builder.push(Token.Placeholder.fromName(name, sql, token_start));
						}
						state = WHITESPACE;
						--i;
					}
					break;

				case STRING:
					if (c == '\'') {
						if (input[i + 1] == '\'') {
							// Single quote escape
							++i;
						} else {
							String value = slice(input, token_start + 1, i).replace("''", "'");
							builder.push(Token.Literal.fromText(value, sql, token_start));
							state = WHITESPACE;
						}
					}
					break;

				case IDENTIFIER:
					if (c == quote_char) {
						String fragment = slice(input, token_start + 1, i);
						builder.push(Token.Identifier.from(fragment, quote_char != '"', sql, token_start));
						state = WHITESPACE;
					}
					break;

				case NUMERIC:
					if (c == '0' && (input[i + 1] == 'x' || input[i + 1] == 'X')) {
						throw new TokenizeException("Hexadecimal integer literals are not supported", sql, token_start);
					}

					boolean has_integer_part = false;
					boolean has_decimal_part = false;
					boolean valid_exponent = true;
					NumericState ns = INTEGER;

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

					String number = String.valueOf(input, token_start, i - token_start);

					if (num_begin == i || (!has_decimal_part && !has_integer_part) || !valid_exponent) {
						throw new TokenizeException("Malformed number", sql, token_start);
					}

					builder.push(Token.Literal.fromNumeric(number, sql, token_start));

					state = WHITESPACE;
					--i;
					break;

				case ALPHA_FRAGMENT:
					if (c != '_' && !Character.isLetter(c) && !Character.isDigit(c)) {
						String fragment = String.valueOf(input, token_start, i - token_start);

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
								operator = Operator.from(fragment).orElse(null);
								break;
						}

						if (operator != null) {
							builder.push(operator.token.at(sql, token_start));
						} else if ((keyword = Keyword.from(fragment).orElse(null)) != null) {
							if (keyword == Keyword.NOT && builder.last().equals(Keyword.IS.token)) {
								builder.pop();
								keyword = Keyword.IS_NOT;
							}
							builder.push(keyword.token.at(sql, token_start));
						} else {
							builder.push(Token.Identifier.from(fragment, false, sql, token_start));
						}
						state = WHITESPACE;
						--i;
					}
					break;

				case SYM_FRAGMENT:
					if (Character.isLetter(c) || Character.isDigit(c) || Character.isWhitespace(c) || i - token_start >= Operator.maxLength) {
						String fragment;
						do {
							fragment = String.valueOf(input, token_start, i - token_start);
							Operator operator = Operator.from(fragment).orElse(null);
							if (operator != null) {
								builder.push(operator.token.at(sql, token_start));
								state = WHITESPACE;
								--i;
								continue outer;
							}
						} while (--i > token_start);
						throw new TokenizeException("Illegal symbol", sql, token_start);
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
			throw new TokenizeException("Unterminated string or identifier", sql, token_start);
		}

		builder.push(Token.EndOfStream.at(sql, length));
		return builder.build();
	}
}
