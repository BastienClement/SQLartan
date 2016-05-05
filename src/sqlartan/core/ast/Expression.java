package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.Tokenizable;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.*;
import static sqlartan.util.Matching.match;

public abstract class Expression implements Node {
	public static Expression parse(ParserContext context) {
		return parseOrStep.parse(context);
	}

	public static Expression parseTerminal(ParserContext context) {
		return match(context.current(), Expression.class)
			.when(Token.Literal.class, lit -> Constant.parse(context))
			.when(Token.Identifier.class, id -> context.alternatives(
				ColumnReference::parse
			))
			.orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	@SafeVarargs
	private static Parser<Expression> parseStep(Parser<Expression> parser, Tokenizable<? extends Token.Wrapper<? extends KeywordOrOperator>>... tokens) {
		return context -> {
			Expression lhs = parser.parse(context);
			KeywordOrOperator op;
			while ((op = consumeAny(context, tokens)) != null) {
				Expression rhs = parser.parse(context);
				lhs = new BinaryOperator(lhs, op, rhs);
			}
			return lhs;
		};
	}

	private static KeywordOrOperator consumeAny(ParserContext context, Tokenizable<? extends Token.Wrapper<? extends KeywordOrOperator>>[] tokens) {
		for (Tokenizable<? extends Token.Wrapper<? extends KeywordOrOperator>> token : tokens) {
			Optional<? extends Token.Wrapper<? extends KeywordOrOperator>> consumed = context.optConsume(token);
			if (consumed.isPresent()) {
				return consumed.get().node();
			}
		}
		return null;
	}

	private static Parser<Expression> parseConcatStep = parseStep(Expression::parseTerminal, CONCAT);
	private static Parser<Expression> parseMulStep = parseStep(parseConcatStep, MUL, DIV, MOD);
	private static Parser<Expression> parseAddStep = parseStep(parseMulStep, PLUS, MINUS);
	private static Parser<Expression> parseBitsStep = parseStep(parseAddStep, SHIFT_LEFT, SHIFT_RIGHT, BIT_AND, BIT_OR);
	private static Parser<Expression> parseCompStep = parseStep(parseBitsStep, LT, LTE, GT, GTE);
	private static Parser<Expression> parseEqStep = parseStep(parseCompStep, EQ, NOT_EQ, IS, IS_NOT, IN, LIKE, GLOB, MATCH, REGEXP);
	private static Parser<Expression> parseAndStep = parseStep(parseEqStep, AND);
	private static Parser<Expression> parseOrStep = parseStep(parseAndStep, OR);

	public static class BinaryOperator extends Expression {
		public Expression lhs;
		public KeywordOrOperator op;
		public Expression rhs;

		public BinaryOperator(Expression lhs, KeywordOrOperator op, Expression rhs) {
			this.lhs = lhs;
			this.op = op;
			this.rhs = rhs;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.append(lhs).append(op).append(rhs);
		}
	}

	public abstract static class Constant extends Expression {
		public String value;

		public Constant() {}
		public Constant(String value) {
			this.value = value;
		}

		public static Constant parse(ParserContext context) {
			return match(context.consume(Token.Literal.class), Constant.class)
				.when(Token.Literal.Text.class, text -> new TextConstant(text.value))
				.when(Token.Literal.Numeric.class, num -> new NumericConstant(num.value))
				.orElseThrow(ParseException.UnexpectedCurrentToken);
		}
	}

	public static class TextConstant extends Constant {
		public TextConstant() {}
		public TextConstant(String value) { super(value); }

		@Override
		public void toSQL(Builder sql) {
			sql.appendTextLiteral(value);
		}
	}

	public static class NumericConstant extends Constant {
		public NumericConstant() {}
		public NumericConstant(String value) { super(value); }

		@Override
		public void toSQL(Builder sql) {
			sql.appendRaw(value);
		}
	}

	public static class ColumnReference extends Expression {
		public String schema;
		public String table;
		public String column;

		public ColumnReference(String schema, String table, String column) {
			this.schema = schema;
			this.table = table;
			this.column = column;
		}

		public static ColumnReference parse(ParserContext context) {
			String table = null, schema = null;

			if (context.next(DOT)) {
				table = context.consumeIdentifier();
				context.consume(DOT);
			}

			if (context.next(DOT)) {
				schema = table;
				table = context.consumeIdentifier();
				context.consume(DOT);
			}

			String column = context.consumeIdentifier();
			return new ColumnReference(schema, table, column);
		}

		@Override
		public void toSQL(Builder sql) {
			if (schema != null)
				sql.appendIdentifier(schema).append(DOT);
			if (table != null)
				sql.appendIdentifier(table).append(DOT);
			sql.appendIdentifier(column);
		}
	}

	public abstract static class RaiseFunction extends Expression {
		public static RaiseFunction parse(ParserContext context) {
			throw new UnsupportedOperationException();
		}
	}
}
