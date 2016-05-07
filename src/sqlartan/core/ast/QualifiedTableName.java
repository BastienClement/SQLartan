package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.Optional;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/syntax/qualified-table-name.html
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class QualifiedTableName implements Node {
	public enum Indexed { Undefined, Indexed, NotIndexed }

	public Optional<String> schema = Optional.empty();
	public String name;
	public Indexed indexed = Indexed.Undefined;
	public String index;

	public static QualifiedTableName parse(ParserContext context) {
		QualifiedTableName table = new QualifiedTableName();
		table.schema = context.optConsumeSchema();
		table.name = context.consumeIdentifier();
		if (context.tryConsume(INDEXED, BY)) {
			table.indexed = Indexed.Indexed;
			table.index = context.consumeIdentifier();
		} else if (context.tryConsume(NOT, INDEXED)) {
			table.indexed = Indexed.NotIndexed;
		}
		return table;
	}

	@Override
	public void toSQL(Builder sql) {
		schema.ifPresent(sql::appendSchema);
		sql.appendIdentifier(name);
		switch (indexed) {
			case Indexed:
				sql.append(INDEXED, BY).appendIdentifier(index);
				break;
			case NotIndexed:
				sql.append(NOT, INDEXED);
				break;
		}
	}
}
