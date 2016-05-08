package sqlartan.core.ast.token;

import org.junit.Test;
import sqlartan.core.ast.StatementList;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;

public class TokenizerTests {
	@Test
	public void tokenizerTests() throws ParseException {
		String source = "SELECT deck_entity.card_id, SUM(deck_entity.number) - IFNULL(owned.number, 0) AS number\n" +
			"            FROM deck\n" +
			"            INNER JOIN (SELECT * FROM card_in_deck UNION ALL SELECT * FROM card_in_side) AS deck_entity\n" +
			"                ON deck_entity.deck_id = deck.deck_id\n" +
			"            LEFT JOIN (\n" +
			"                SELECT DISTINCT card_id,\n" +
			"                normal + foil AS number\n" +
			"                FROM card_in_collection WHERE user_id = :user_id\n" +
			"            ) AS owned\n" +
			"                ON owned.card_id = deck_entity.card_id\n" +
			"            WHERE name = :deck_name AND user_id = :user_id\n" +
			"            GROUP BY deck_entity.card_id, owned.number\n" +
			"            HAVING owned.number IS NULL OR SUM(deck_entity.number) > owned.number";

		TokenSource ts = TokenSource.from(source);
		ts.tokens().forEach(System.out::println);

		System.out.print("\n");

		StatementList statementList = Parser.parse(source, StatementList::parse);
		System.out.println(statementList.toSQL());
	}
}
