package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserBuilder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import static sqlartan.core.ast.token.Operator.SEMICOLON;

public class StatementList extends ArrayList<Statement> implements Node {
	public static final ParserBuilder<StatementList> parser = new ParserBuilder<>(StatementList::parse);

	static StatementList parse(ParserContext context) {
		StatementList statementList = new StatementList();
		context.parseList(statementList, SEMICOLON, Statement::parse);
		return statementList;
	}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append(this, "; ");
		if (!isEmpty())
			sql.append(";");
	}
}
