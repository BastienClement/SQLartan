package sqlartan.core.ast.token;

import org.junit.Test;
import sqlartan.core.ast.StatementList;
import sqlartan.core.ast.parser.ParseException;

public class TokenizerTests {
	@Test
	public void tokenizerTests() throws ParseException {
		String source = "select distinct *, foo.*, 'a', 2.33e+5, main.foo.bar AS houray from main.foo AS b;";

		TokenSource ts = TokenSource.from(source);
		ts.tokens.forEach(System.out::println);

		System.out.print("\n");

		StatementList statementList = StatementList.parser.parse(source);
		System.out.println(statementList.toSQL());
	}
}
