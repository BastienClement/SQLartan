package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Operator.DOT;
import static sqlartan.util.Matching.match;

public interface Expression extends Node {
	static Expression parse(ParserContext context) {
		return context.alternatives(
			() -> context.parse(Expression.Literal::parse),
			() -> context.parse(ColumnReference::parse),
			() -> context.parse(RaiseFunction::parse)
		);
	}

	abstract class Literal implements Expression {
		public String value;

		public Literal() {}
		public Literal(String value) {
			this.value = value;
		}

		static Literal parse(ParserContext context) {
			return match(context.consume(sqlartan.core.ast.token.Literal.class), Literal.class)
				.when(sqlartan.core.ast.token.Literal.Text.class, text -> new TextLiteral(text.value))
				.when(sqlartan.core.ast.token.Literal.Numeric.class, num -> new NumericLiteral(num.value))
				.orElseThrow(ParseException.UnexpectedCurrentToken);
		}
	}

	class TextLiteral extends Literal {
		public TextLiteral() {}
		public TextLiteral(String value) { super(value); }

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.appendTextLiteral(value);
		}
	}

	class NumericLiteral extends Literal {
		public NumericLiteral() {}
		public NumericLiteral(String value) { super(value); }

		@Override
		public void toSQL(SQLBuilder sql) {
			sql.append(value);
		}
	}

	class ColumnReference implements Expression {
		public String schema;
		public String table;
		public String column;

		public ColumnReference(String schema, String table, String column) {
			this.schema = schema;
			this.table = table;
			this.column = column;
		}

		static ColumnReference parse(ParserContext context) {
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

	abstract class RaiseFunction implements Expression {
		static RaiseFunction parse(ParserContext context) {
			throw new UnsupportedOperationException();
		}
	}
}
