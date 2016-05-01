package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.VACUUM;

public class VacuumStatement extends Statement {
	public static final VacuumStatement singleton = new VacuumStatement();
	public static final ParserBuilder<VacuumStatement> parser = new ParserBuilder<>(VacuumStatement::parse);

	public static VacuumStatement parse(ParserContext context) {
		context.consume(VACUUM);
		return singleton;
	}

	private VacuumStatement() {}

	@Override
	public void toSQL(StringBuilder sb) {
		sb.append("VACUUM");
	}
}
