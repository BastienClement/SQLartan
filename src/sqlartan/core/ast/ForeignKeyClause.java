package sqlartan.core.ast;

import sqlartan.core.ast.gen.Buildable;
import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.LEFT_PAREN;
import static sqlartan.core.ast.Operator.RIGHT_PAREN;

/**
 * https://www.sqlite.org/syntax/foreign-key-clause.html
 * https://www.sqlite.org/foreignkeys.html
 */
@SuppressWarnings("WeakerAccess")
public class ForeignKeyClause implements Node {
	public enum Action {Undefined, NoAction, SetNull, SetDefault, Cascade, Restrict}
	public enum Deferrable {Undefined, Deferrable, NotDeferrable}
	public enum Initially {Undefined, Deferred, Immediate}

	public String table;
	public List<String> columns = new ArrayList<>();
	public Action onDelete = Action.Undefined;
	public Action onUpdate = Action.Undefined;
	public Deferrable deferrable = Deferrable.Undefined;
	public Initially initially = Initially.Undefined;

	public static ForeignKeyClause parse(ParserContext context) {
		ForeignKeyClause fk = new ForeignKeyClause();
		context.consume(REFERENCES);
		fk.table = context.consumeIdentifier();

		if (context.tryConsume(LEFT_PAREN)) {
			fk.columns = context.parseList(ParserContext::consumeIdentifier);
			context.consume(RIGHT_PAREN);
		}

		for (int i = 0; i < 2; i++) {
			if (context.tryConsume(ON)) {
				if (fk.onDelete == Action.Undefined && context.tryConsume(DELETE)) {
					fk.onDelete = parseAction(context);
				} else if (fk.onUpdate == Action.Undefined && context.tryConsume(UPDATE)) {
					fk.onUpdate = parseAction(context);
				} else {
					// Double ON DELETE / ON UPDATE
					throw ParseException.UnexpectedCurrentToken;
				}
			} else if (context.tryConsume(MATCH)) {
				// TODO: parse MATCH
				throw new UnsupportedOperationException();
			} else {
				break;
			}
		}

		if (context.tryConsume(NOT, DEFERRABLE)) {
			fk.deferrable = Deferrable.NotDeferrable;
		} else if (context.tryConsume(DEFERRABLE)) {
			fk.deferrable = Deferrable.Deferrable;
		}

		if (fk.deferrable != Deferrable.Undefined) {
			if (context.tryConsume(INITIALLY)) {
				if (context.tryConsume(DEFERRED)) {
					fk.initially = Initially.Deferred;
				} else {
					context.consume(IMMEDIATE);
					fk.initially = Initially.Immediate;
				}
			}
		}

		return fk;
	}

	private static Action parseAction(ParserContext context) {
		if (context.tryConsume(SET, NULL)) {
			return Action.SetNull;
		} else if (context.tryConsume(SET, DEFAULT)) {
			return Action.SetDefault;
		} else if (context.tryConsume(CASCADE)) {
			return Action.Cascade;
		} else if (context.tryConsume(RESTRICT)) {
			return Action.Restrict;
		} else {
			context.consume(NO, ACTION);
			return Action.NoAction;
		}
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(REFERENCES).appendIdentifier(table);

		if (!columns.isEmpty()) {
			sql.append(LEFT_PAREN).appendIdentifiers(columns).append(RIGHT_PAREN);
		}

		sql.append(actionToSQL(onDelete, DELETE))
		   .append(actionToSQL(onUpdate, UPDATE));

		if (deferrable != Deferrable.Undefined) {
			if (deferrable == Deferrable.NotDeferrable) sql.append(NOT);
			sql.append(DEFERRABLE);
			if (initially != Initially.Undefined) {
				sql.append(INITIALLY, initially == Initially.Deferred ? DEFERRED : IMMEDIATE);
			}
		}
	}

	private Buildable actionToSQL(Action action, Keyword prefix) {
		return sql -> {
			if (action == Action.Undefined) return;
			sql.append(ON, prefix);
			switch (action) {
				case SetNull:
					sql.append(SET, NULL);
					break;
				case SetDefault:
					sql.append(SET, DEFAULT);
					break;
				case Cascade:
					sql.append(CASCADE);
					break;
				case Restrict:
					sql.append(RESTRICT);
					break;
				case NoAction:
					sql.append(NO, ACTION);
					break;
			}
		};
	}
}
