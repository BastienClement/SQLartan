package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.ArrayList;
import static sqlartan.core.ast.Operator.SEMICOLON;

/**
 * https://www.sqlite.org/syntax/sql-stmt-list.html
 */
public class StatementList extends ArrayList<Statement> implements Node {
	public static StatementList parse(ParserContext context) {
		StatementList statementList = new StatementList();
		do {
			if (context.current(Token.EndOfStream.class)) return statementList;
			statementList.add(Statement.parse(context));
		} while (context.tryConsume(SEMICOLON));
		return statementList;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(this, SEMICOLON);
		if (!isEmpty())
			sql.append(SEMICOLON);
	}
}
