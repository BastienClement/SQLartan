package sqlartan.core.ast.token;

import org.junit.Test;

public class TokenizerTests {
	@Test
	public void tokenizerTests() {
		TokenSource source = Tokenizer.tokenize("SeLeCt * FROM [f].[o],o where `a` = -2.5e-990 << 4 AND \"b\" IS NOT 'foo''bar'");
		source.tokens.forEach(System.out::println);
	}
}
