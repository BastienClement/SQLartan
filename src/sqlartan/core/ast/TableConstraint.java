package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class TableConstraint implements Node {
	public static TableConstraint parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
