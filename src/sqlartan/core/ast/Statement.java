package sqlartan.core.ast;

import sqlartan.core.ast.token.TokenSource;
import static sqlartan.core.ast.token.Keyword.*;
import static sqlartan.util.Matching.match;

public abstract class Statement {
	public static Statement parse(TokenSource source) {
		return match(source.current(), Statement.class)
				.when(null, () -> null)
				.when(EXPLAIN, () -> ExplainStatement.parse(source))
				.when(ALTER, () -> AlterTableStatement.parse(source))
				.when(ANALYZE, () -> AnalyzeStatement.parse(source))
				.when(ATTACH, () -> AttachStatement.parse(source))
				.when(BEGIN, () -> BeginStatement.parse(source))
				.when(COMMIT, () -> CommitStatement.parse(source))
				.when(END, () -> CommitStatement.parse(source))
				.when(CREATE, () -> CreateStatement.parse(source))
				.when(DETACH, () -> DetachStatement.parse(source))
				.when(DROP, () -> DropStatement.parse(source))
				.when(PRAGMA, () -> PragmaStatement.parse(source))
				.when(REINDEX, () -> ReindexStatement.parse(source))
				.when(RELEASE, () -> ReleaseStatement.parse(source))
				.when(ROLLBACK, () -> RollbackStatement.parse(source))
				.when(SAVEPOINT, () -> SavepointStatement.parse(source))
				.when(VACUUM, () -> VacuumStatement.parse(source))
				.orElse(() -> {
					throw new UnsupportedOperationException();
					/*UpdateStatement.parse(source);
					DeleteStatement.parse(source);
					InsertStatement.parse(source);
					return SelectStatement.parse(source);*/
				});
	}
}
