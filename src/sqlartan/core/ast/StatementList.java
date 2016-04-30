package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.Operator;
import sqlartan.core.ast.token.TokenSource;
import java.util.ArrayList;

public class StatementList extends ArrayList<Statement> {
	public static StatementList parse(String sql) throws ParseException {
		return parse(TokenSource.from(sql));
	}

	public static StatementList parse(TokenSource source) {
		StatementList statementList = new StatementList();

		do {
			Statement statement = Statement.parse(source);
			if (statement == null) break;
			statementList.add(statement);
		} while (source.consume(Operator.SEMICOLON));

		return statementList;
	}
}
