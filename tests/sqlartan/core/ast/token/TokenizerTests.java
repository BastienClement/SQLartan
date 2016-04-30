package sqlartan.core.ast.token;

import org.junit.Test;
import sqlartan.core.ast.StatementList;

public class TokenizerTests {
	@Test
	public void tokenizerTests() throws TokenizeException {
		TokenSource source = TokenSource.from("SELECT * FROM [table]");
		source.tokens.forEach(System.out::println);

		StatementList statementList = StatementList.parse(source);
		statementList.forEach(System.out::println);
	}
}
