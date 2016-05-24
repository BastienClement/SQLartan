package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.LEFT_PAREN;
import static sqlartan.core.ast.Operator.RIGHT_PAREN;

/**
 * https://www.sqlite.org/lang_insert.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class InsertStatement implements Statement {
	public enum Type implements Node.Enumerated {
		Insert(INSERT), Replace(REPLACE),
		InsertOrReplace(INSERT, OR, REPLACE),
		InsertOrRollback(INSERT, OR, ROLLBACK),
		InsertOrAbort(INSERT, OR, ABORT),
		InsertOrFail(INSERT, OR, FAIL),
		InsertOrIgnore(INSERT, OR, IGNORE);

		private Keyword[] keywords;

		Type(Keyword... keywords) {
			this.keywords = keywords;
		}

		public static Type parse(ParserContext context) {
			if (context.tryConsume(REPLACE)) {
				return Replace;
			} else {
				context.consume(INSERT);
				if (context.tryConsume(OR)) {
					switch (context.consume(Token.Keyword.class).node()) {
						case REPLACE:
							return InsertOrReplace;
						case ROLLBACK:
							return InsertOrReplace;
						case ABORT:
							return InsertOrAbort;
						case FAIL:
							return InsertOrFail;
						case IGNORE:
							return InsertOrIgnore;
						default:
							throw ParseException.UnexpectedCurrentToken(REPLACE, ROLLBACK, ABORT, FAIL, IGNORE);
					}
				}
				return Insert;
			}
		}

		@Override
		public void toSQL(Builder sql) {
			sql.append(keywords);
		}
	}

	public Type type;
	public Optional<String> schema;
	public String table;
	public List<String> columns = new ArrayList<>();

	public static InsertStatement parse(ParserContext context) {
		Type type = Type.parse(context);
		Optional<String> schema = context.optConsumeSchema();

		context.consume(INTO);
		String table = context.consumeIdentifier();
		List<String> columns = new ArrayList<>();
		if (context.tryConsume(LEFT_PAREN)) {
			columns = context.parseList(ParserContext::consumeIdentifier);
			context.consume(RIGHT_PAREN);
		}

		InsertStatement insert;

		if (context.current(DEFAULT)) {
			insert = Default.parse(context);
		} else {
			insert = Select.parse(context);
		}

		insert.type = type;
		insert.schema = schema;
		insert.table = table;
		insert.columns = columns;

		return insert;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(type).append(INTO)
		   .appendSchema(schema).appendIdentifier(table);
		if (!columns.isEmpty()) {
			sql.append(LEFT_PAREN).appendIdentifiers(columns).append(RIGHT_PAREN);
		}
	}

	/**
	 * INSERT INTO ... [SELECT | VALUES] ... ;
	 */
	public static class Select extends InsertStatement {
		public SelectStatement select;

		public static Select parse(ParserContext context) {
			Select select = new Select();
			select.select = SelectStatement.parse(context);
			return select;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(select);
		}
	}

	/**
	 * INSERT INTO ... DEFAULT VALUES ;
	 */
	public static class Default extends InsertStatement {
		public static final Default instance = new Default();
		private Default() {}

		public static Default parse(ParserContext context) {
			context.consume(DEFAULT, VALUES);
			return instance;
		}

		@Override
		public void toSQL(Builder sql) {
			super.toSQL(sql);
			sql.append(DEFAULT, VALUES);
		}
	}
}
