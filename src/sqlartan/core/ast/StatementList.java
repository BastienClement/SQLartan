package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import static sqlartan.core.ast.token.EndOfStream.EOS;
import static sqlartan.core.ast.token.Operator.SEMICOLON;

public class StatementList extends ArrayList<Statement> implements Node {
	public static StatementList parse(ParserContext context) {
		StatementList statementList = new StatementList();
		do {
			if (context.current(EOS)) return statementList;
			statementList.add(Statement.parse(context));
		} while(context.tryConsume(SEMICOLON));
		return statementList;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(this, "; ");
		if (!isEmpty())
			sql.append(";");
	}
}
