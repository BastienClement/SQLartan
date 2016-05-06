package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.Keyword.*;

/**
 * https://www.sqlite.org/lang_attach.html
 */
@SuppressWarnings("WeakerAccess")
public class AttachStatement implements Statement {
	public Expression file;
	public String schema;

	public static AttachStatement parse(ParserContext context) {
		context.consume(ATTACH);
		context.tryConsume(DATABASE);
		AttachStatement attach = new AttachStatement();
		attach.file = Expression.parse(context);
		context.consume(AS);
		attach.schema = context.consumeIdentifier();
		return attach;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(ATTACH)
		   .append(file)
		   .append(AS)
		   .appendIdentifier(schema);
	}
}
