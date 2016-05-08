package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.Token;
import java.util.Optional;
import static sqlartan.core.ast.Operator.*;

/**
 * https://www.sqlite.org/syntaxdiagrams.html#type-name
 */
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "WeakerAccess" })
public class TypeDefinition implements Node {
	public String name;
	public Optional<LiteralValue.Numeric> length = Optional.empty();
	public Optional<LiteralValue.Numeric> scale = Optional.empty();

	public static TypeDefinition parse(ParserContext context) {
		TypeDefinition type = new TypeDefinition();
		type.name = context.consumeIdentifier();

		// SQLite accepts multiple type parts but only stores the first one ?!
		// TODO: check what the fuck ?!
		//noinspection StatementWithEmptyBody
		while (context.tryConsume(Token.Identifier.class)) ;

		if (context.tryConsume(LEFT_PAREN)) {
			type.length = Optional.of(LiteralValue.Numeric.parse(context));
			if (context.tryConsume(COMMA)) {
				type.scale = Optional.of(LiteralValue.Numeric.parse(context));
			}
			context.consume(RIGHT_PAREN);
		}

		return type;
	}

	@Override
	public void toSQL(Builder sql) {
		sql.appendIdentifier(name);
		length.ifPresent(l -> {
			sql.append(LEFT_PAREN).appendRaw(l.value);
			scale.ifPresent(s -> sql.append(COMMA).appendRaw(s.value));
			sql.append(RIGHT_PAREN);
		});
	}
}
