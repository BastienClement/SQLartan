package sqlartan.core.ast.token;

public enum Type {
	KEYWORD("Keyword"),
	OPERATOR("Operator"),
	IDENTIFIER("Identifier"),
	LITERAL("Literal"),
	PLACEHOLDER("Placeholder"),
	END_OF_STREAM("EndOfStream");

	public final String label;

	Type(String label) {
		this.label = label;
	}
}
