package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.parser.ParserContext;
import java.util.ArrayList;
import java.util.List;
import static sqlartan.core.ast.Keyword.ON;
import static sqlartan.core.ast.Keyword.USING;
import static sqlartan.core.ast.Operator.LEFT_PAREN;
import static sqlartan.core.ast.Operator.RIGHT_PAREN;

/**
 * https://www.sqlite.org/syntax/join-constraint.html
 */
@SuppressWarnings("WeakerAccess")
public abstract class JoinConstraint implements Node {
	public static JoinConstraint parse(ParserContext context) {
		if (context.current(ON)) {
			return On.parse(context);
		} else {
			return Using.parse(context);
		}
	}

	/**
	 * JOIN ... ON ...
	 */
	public static class On extends JoinConstraint {
		public Expression expression;

		public static On parse(ParserContext context) {
			context.consume(ON);
			On on = new On();
			on.expression = Expression.parse(context);
			return on;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.append(ON).append(expression);
		}
	}

	/**
	 * JOIN ... USING ( ... ) ...
	 */
	public static class Using extends JoinConstraint {
		public List<String> columns = new ArrayList<>();

		public static Using parse(ParserContext context) {
			context.consume(USING, LEFT_PAREN);
			Using using = new Using();
			using.columns = context.parseList(ParserContext::consumeIdentifier);
			context.consume(RIGHT_PAREN);
			return using;
		}

		@Override
		public void toSQL(Builder sql) {
			sql.append(USING, LEFT_PAREN).appendIdentifiers(columns).append(RIGHT_PAREN);
		}
	}
}
