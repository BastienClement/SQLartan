package sqlartan.core.ast.token;

import org.junit.Test;
import sqlartan.core.ast.Statement;
import sqlartan.core.ast.StatementList;

public class TokenizerTests {
	@Test
	public void tokenizerTests() {
		StatementList statementList = StatementList.parse("VACUUM; VACUUM;");
		for (Statement statement : statementList) {
			System.out.println(statement);
		}
	}
}
