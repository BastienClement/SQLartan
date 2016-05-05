package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.util.Matching.match;

public interface Statement extends Node {
	static Statement parse(ParserContext context) {
		return match(context.current(), Statement.class)
			.when(EXPLAIN, () -> ExplainStatement.parse(context))
			.when(ALTER, () -> AlterTableStatement.parse(context))
			.when(ANALYZE, () -> AnalyzeStatement.parse(context))
			.when(ATTACH, () -> AttachStatement.parse(context))
			.when(BEGIN, () -> BeginStatement.parse(context))
			.when(COMMIT, () -> CommitStatement.parse(context))
			.when(END, () -> CommitStatement.parse(context))
			.when(CREATE, () -> CreateStatement.parse(context))
			.when(DELETE, () -> DeleteStatement.parse(context))
			.when(DETACH, () -> DetachStatement.parse(context))
			.when(DROP, () -> DropStatement.parse(context))
			.when(INSERT, () -> InsertStatement.parse(context))
			.when(REPLACE, () -> InsertStatement.parse(context))
			.when(PRAGMA, () -> PragmaStatement.parse(context))
			.when(REINDEX, () -> ReindexStatement.parse(context))
			.when(RELEASE, () -> ReleaseStatement.parse(context))
			.when(ROLLBACK, () -> RollbackStatement.parse(context))
			.when(SAVEPOINT, () -> SavepointStatement.parse(context))
			.when(SELECT, () -> SelectStatement.parse(context))
			.when(UPDATE, () -> UpdateStatement.parse(context))
			.when(VACUUM, () -> VacuumStatement.parse(context))
			.when(WITH, () -> {
				throw new UnsupportedOperationException();
			})
			.orElseThrow(ParseException.UnexpectedCurrentToken);
		// Note: This does not handle INSERT / UPDATE / DELETE / INSERT with WITH clauses
	}
}
