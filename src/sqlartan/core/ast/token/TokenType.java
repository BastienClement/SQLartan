package sqlartan.core.ast.token;

public enum TokenType {
	KEYWORD("Keyword"),
	OPERATOR("Operator"),
	IDENTIFIER("Identifier"),
	LITERAL("Literal"),
	PLACEHOLDER("Placeholder"),
	END_OF_STREAM("EndOfStream");

	public final String label;

	TokenType(String label) {
		this.label = label;
	}
}
