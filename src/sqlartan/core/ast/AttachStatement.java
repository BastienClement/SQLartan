package sqlartan.core.ast;

import sqlartan.core.ast.gen.SQLBuilder;
import sqlartan.core.ast.parser.ParserContext;
import static sqlartan.core.ast.token.Keyword.AS;
import static sqlartan.core.ast.token.Keyword.ATTACH;
import static sqlartan.core.ast.token.Keyword.DATABASE;

/**
 * https://www.sqlite.org/lang_attach.html
 */
public class AttachStatement implements Statement {
	public Expression file;
	public String schema;

	public static AttachStatement parse(ParserContext context) {
		context.consume(ATTACH);
		context.tryConsume(DATABASE);
		AttachStatement attach = new AttachStatement();
		attach.file = context.parse(Expression::parse);
		context.consume(AS);
		attach.schema = context.consumeIdentifier().value;
		return attach;
	}

	@Override
	public void toSQL(SQLBuilder sql) {
		sql.append("ATTACH ").append(file).append(" AS ").appendIdentifier(schema);
	}
}
