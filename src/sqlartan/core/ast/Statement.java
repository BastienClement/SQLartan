package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.util.Matching.match;

public abstract class Statement {
	public static Statement parse(ParserContext context) {
		return match(context.current(), Statement.class)
				.when(null, () -> null)
				.when(EXPLAIN, () -> context.parse(ExplainStatement::parse))
				.when(ALTER, () -> context.parse(AlterTableStatement::parse))
				.when(ANALYZE, () -> context.parse(AnalyzeStatement::parse))
				.when(ATTACH, () -> context.parse(AttachStatement::parse))
				.when(BEGIN, () -> context.parse(BeginStatement::parse))
				.when(COMMIT, () -> context.parse(CommitStatement::parse))
				.when(END, () -> context.parse(CommitStatement::parse))
				.when(CREATE, () -> context.parse(CreateStatement::parse))
				.when(DELETE, () -> context.parse(DeleteStatement::parse))
				.when(DETACH, () -> context.parse(DetachStatement::parse))
				.when(DROP, () -> context.parse(DropStatement::parse))
				.when(INSERT, () -> context.parse(InsertStatement::parse))
				.when(REPLACE, () -> context.parse(InsertStatement::parse))
				.when(PRAGMA, () -> context.parse(PragmaStatement::parse))
				.when(REINDEX, () -> context.parse(ReindexStatement::parse))
				.when(RELEASE, () -> context.parse(ReleaseStatement::parse))
				.when(ROLLBACK, () -> context.parse(RollbackStatement::parse))
				.when(SAVEPOINT, () -> context.parse(SavepointStatement::parse))
				.when(SELECT, () -> context.parse(SelectStatement::parse))
				.when(UPDATE, () -> context.parse(UpdateStatement::parse))
				.when(VACUUM, () -> context.parse(VacuumStatement::parse))
				.orElseThrow(ParseException.UnexpectedCurrentToken);
	}
}
