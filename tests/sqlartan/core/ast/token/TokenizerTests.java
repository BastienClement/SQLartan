package sqlartan.core.ast.token;

import org.junit.Test;
import sqlartan.core.ast.StatementList;
import sqlartan.core.ast.parser.ParseException;

public class TokenizerTests {
	@Test
	public void tokenizerTests() throws ParseException {
		String source = "VACUUM; VACUUM;";

		//TokenSource ts = TokenSource.from(source);
		//ts.tokens.forEach(System.out::println);

		StatementList statementList = StatementList.parser.parse(source);
		statementList.forEach(System.out::println);
	}
}
