package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_transaction.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class RollbackStatement implements Statement {
	public Optional<String> savepoint = Optional.empty();

	public static RollbackStatement parse(ParserContext context) {
		context.consume(ROLLBACK);
		context.tryConsume(TRANSACTION);

		RollbackStatement rollback = new RollbackStatement();

		if (context.tryConsume(TO)) {
			context.tryConsume(SAVEPOINT);
			rollback.savepoint = Optional.of(context.consumeIdentifier());
		}

		return rollback;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(ROLLBACK);
		savepoint.ifPresent(s -> sql.append(TO).appendIdentifier(s));
	}
}
