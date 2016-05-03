package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Operator.DOT;
import static sqlartan.util.Matching.match;

public abstract class Expression implements Node {
	public static Expression parse(ParserContext context) {
		return context.alternatives(
			() -> context.parse(Expression.Literal::parse),
			() -> context.parse(ColumnReference::parse),
			() -> context.parse(RaiseFunction::parse)
		);
	}

	public abstract static class Literal extends Expression {
		public String value;

		public Literal() {}
		public Literal(String value) {
			this.value = value;
		}

		public static Literal parse(ParserContext context) {
			return match(context.consume(sqlartan.core.ast.token.Literal.class), Literal.class)
				.when(sqlartan.core.ast.token.Literal.Text.class, text -> new TextLiteral(text.value))
				.when(sqlartan.core.ast.token.Literal.Numeric.class, num -> new NumericLiteral(num.value))
				.orElseThrow(ParseException.UnexpectedCurrentToken);
		}
	}

	public static class TextLiteral extends Literal {
		public TextLiteral() {}
		public TextLiteral(String value) { super(value); }

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.appendTextLiteral(value);
		}
	}

	public static class NumericLiteral extends Literal {
		public NumericLiteral() {}
		public NumericLiteral(String value) { super(value); }

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
