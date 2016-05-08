package sqlartan.core.ast.token;

import org.junit.Test;
import sqlartan.core.ast.StatementList;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;

public class TokenizerTests {
	@Test
	public void tokenizerTests() throws ParseException {
		String source = "SELECT 1, NULL, 'foobar', ?, foo.bar.baz, NOT NULL";

		TokenSource ts = TokenSource.from(source);
		ts.tokens().forEach(System.out::println);

		System.out.print("\n");

		StatementList statementList = Parser.parse(source, StatementList::parse);
		System.out.println(statementList.toSQL());
	}
}
