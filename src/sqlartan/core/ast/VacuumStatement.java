package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.VACUUM;

public class VacuumStatement implements Statement {
	public static final VacuumStatement instance = new VacuumStatement();
	public static final ParserBuilder<VacuumStatement> parser = new ParserBuilder<>(VacuumStatement::parse);

	public static VacuumStatement parse(ParserContext context) {
		context.consume(VACUUM);
		return instance;
	}

	private VacuumStatement() {}

	@Override
	public void toSQL(Builder sql) {
		sql.append("VACUUM");
	}
}
