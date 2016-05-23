package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.VOID;

/**
 * https://www.sqlite.org/syntaxdiagrams.html#column-def
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class ColumnDefinition implements Node {
	public String name;
	public Optional<TypeDefinition> type = Optional.empty();
	public List<ColumnConstraint> constraints = new ArrayList<>();

	public static ColumnDefinition parse(ParserContext context) {
		ColumnDefinition column = new ColumnDefinition();
		column.name = context.consumeIdentifier();
		column.type = context.optParse(TypeDefinition::parse);
		context.parseList(column.constraints, VOID, ColumnConstraint::parse);
		return column;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.appendIdentifier(name)
		   .append(type)
		   .append(constraints, VOID);
	}
}
