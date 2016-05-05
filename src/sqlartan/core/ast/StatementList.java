package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserBuilder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import static sqlartan.core.ast.token.EndOfStream.EOS;
import static sqlartan.core.ast.token.Operator.SEMICOLON;

public class StatementList extends ArrayList<Statement> implements Node {
	public static final ParserBuilder<StatementList> parser = new ParserBuilder<>(StatementList::parse);

	static StatementList parse(ParserContext context) {
		StatementList statementList = new StatementList();
		do {
			if (context.current(EOS)) return statementList;
			statementList.add(context.parse(Statement::parse));
		} while(context.tryConsume(SEMICOLON));
		return statementList;
	}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append(this, "; ");
		if (!isEmpty())
			sql.append(";");
	}
}
