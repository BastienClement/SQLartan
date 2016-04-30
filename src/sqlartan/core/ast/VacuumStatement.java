package sqlartan.core.ast;

import sqlartan.core.ast.token.Keyword;
import sqlartan.core.ast.token.TokenSource;

public class VacuumStatement extends Statement {
	public static final VacuumStatement Singleton = new VacuumStatement();

	public static VacuumStatement parse(TokenSource source) {
		return (source.consume(Keyword.VACUUM)) ? Singleton : null;
	}

	private VacuumStatement() {
	}
}
