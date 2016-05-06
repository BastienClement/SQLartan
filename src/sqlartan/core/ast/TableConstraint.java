package sqlartan.core.ast;

import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public abstract class TableConstraint implements Node {
	public Optional<String> name;


	public static TableConstraint parse(ParserContext context) {
		throw new UnsupportedOperationException();
	}
}
