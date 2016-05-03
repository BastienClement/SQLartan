package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Identifier;
import sqlartan.core.ast.token.Literal;
import sqlartan.core.ast.token.Token;
import java.util.Optional;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.core.ast.token.Operator.*;
import static sqlartan.util.Matching.match;

public abstract class Expression implements Node {
	public static Expression parse(ParserContext context) {
		return context.parse(parseOrStep);
	}

	public static Expression parseTerminal(ParserContext context) {
		return match(context.current(), Expression.class)
			.when(Literal.class, lit -> context.parse(Constant::parse))
			.when(Identifier.class, id -> context.alternatives(
				() -> context.parse(ColumnReference::parse)
			))
			.orElseThrow(ParseException.UnexpectedCurrentToken);
	}

	private static Parser<Expression> parseStep(Parser<Expression> parser, Token<?>... tokens) {
		return context -> {
			Expression lhs = context.parse(parser);
			Token<?> op;
			while ((op = consumeAny(context, tokens)) != null) {
				Expression rhs = context.parse(parser);
				lhs = new BinaryOperator(lhs, op, rhs);
			}
			return lhs;
		};
	}

	private static Token<?> consumeAny(ParserContext context, Token<?>[] tokens) {
		for (Token<?> token : tokens) {
			Optional<Token<?>> consumed = context.optConsume(token);
			if (consumed.isPresent()) {
				return consumed.get();
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
		public Token<?> op;
		public Expression rhs;

		public BinaryOperator(Expression lhs, Token<?> op, Expression rhs) {
			this.lhs = lhs;
			this.op = op;
			this.rhs = rhs;
		}

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.append(lhs).append(" ").append(op.stringValue()).append(" ").append(rhs);
		}
	}

	public abstract static class Constant extends Expression {
		public String value;

		public Constant() {}
		public Constant(String value) {
			this.value = value;
		}

		public static Constant parse(ParserContext context) {
			return match(context.consume(Literal.class), Constant.class)
				.when(Literal.Text.class, text -> new TextConstant(text.value))
				.when(Literal.Numeric.class, num -> new NumericConstant(num.value))
				.orElseThrow(ParseException.UnexpectedCurrentToken);
		}
	}

	public static class TextConstant extends Constant {
		public TextConstant() {}
		public TextConstant(String value) { super(value); }

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.appendTextLiteral(value);
		}
	}

	public static class NumericConstant extends Constant {
		public NumericConstant() {}
		public NumericConstant(String value) { super(value); }

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.append(value);
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
				table = context.consumeIdentifier().value;
				context.consume(DOT);
			}

			if (context.next(DOT)) {
				schema = table;
				table = context.consumeIdentifier().value;
				context.consume(DOT);
			}

			String column = context.consumeIdentifier().value;
			return new ColumnReference(schema, table, column);
		}

		@Override
		public void toSQL(SQLBuilder sql) {
			if (schema != null)
				sql.appendIdentifier(schema).append(".");
			if (table != null)
				sql.appendIdentifier(table).append(".");
			sql.appendIdentifier(column);
		}
	}

	public abstract static class RaiseFunction extends Expression {
		public static RaiseFunction parse(ParserContext context) {
			throw new UnsupportedOperationException();
		}
	}
}
