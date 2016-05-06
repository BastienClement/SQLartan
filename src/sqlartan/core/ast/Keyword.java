package sqlartan.core.ast;

import sqlartan.core.ast.gen.Builder;
import sqlartan.core.ast.token.Token;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SQL language keywords
 */
public enum Keyword implements Node.KeywordOrOperator {
	ABORT("ABORT"), ACTION("ACTION"), ADD("ADD"), AFTER("AFTER"), ALL("ALL"), ALTER("ALTER"), ANALYZE("ANALYZE"),
	AND("AND"), AS("AS"), ASC("ASC"), ATTACH("ATTACH"), AUTOINCREMENT("AUTOINCREMENT"), BEFORE("BEFORE"),
	BEGIN("BEGIN"), BETWEEN("BETWEEN"), BY("BY"), CASCADE("CASCADE"), CASE("CASE"), CAST("CAST"), CHECK("CHECK"),
	COLLATE("COLLATE"), COLUMN("COLUMN"), COMMIT("COMMIT"), CONFLICT("CONFLICT"), CONSTRAINT("CONSTRAINT"),
	CREATE("CREATE"), CROSS("CROSS"), CURRENT_DATE("CURRENT_DATE"), CURRENT_TIME("CURRENT_TIME"),
	CURRENT_TIMESTAMP("CURRENT_TIMESTAMP"), DATABASE("DATABASE"), DEFAULT("DEFAULT"), DEFERRABLE("DEFERRABLE"),
	DEFERRED("DEFERRED"), DELETE("DELETE"), DESC("DESC"), DETACH("DETACH"), DISTINCT("DISTINCT"), DROP("DROP"),
	EACH("EACH"), ELSE("ELSE"), END("END"), ESCAPE("ESCAPE"), EXCEPT("EXCEPT"), EXCLUSIVE("EXCLUSIVE"),
	EXISTS("EXISTS"), EXPLAIN("EXPLAIN"), FAIL("FAIL"), FOR("FOR"), FOREIGN("FOREIGN"), FROM("FROM"), FULL("FULL"),
	GLOB("GLOB"), GROUP("GROUP"), HAVING("HAVING"), IF("IF"), IGNORE("IGNORE"), IMMEDIATE("IMMEDIATE"), IN("IN"),
	INDEX("INDEX"), INDEXED("INDEXED"), INITIALLY("INITIALLY"), INNER("INNER"), INSERT("INSERT"), INSTEAD("INSTEAD"),
	INTERSECT("INTERSECT"), INTO("INTO"), IS("IS"), IS_NOT("IS NOT"), ISNULL("ISNULL"), JOIN("JOIN"), KEY("KEY"),
	LEFT("LEFT"), LIKE("LIKE"), LIMIT("LIMIT"), MATCH("MATCH"), NATURAL("NATURAL"), NO("NO"), NOT("NOT"),
	NOTNULL("NOTNULL"), NULL("NULL"), OF("OF"), OFFSET("OFFSET"), ON("ON"), OR("OR"), ORDER("ORDER"), OUTER("OUTER"),
	PLAN("PLAN"), PRAGMA("PRAGMA"), PRIMARY("PRIMARY"), QUERY("QUERY"), RAISE("RAISE"), RECURSIVE("RECURSIVE"),
	REFERENCES("REFERENCES"), REGEXP("REGEXP"), REINDEX("REINDEX"), RELEASE("RELEASE"), RENAME("RENAME"),
	REPLACE("REPLACE"), RESTRICT("RESTRICT"), RIGHT("RIGHT"), ROLLBACK("ROLLBACK"), ROW("ROW"), ROWID("ROWID"),
	SAVEPOINT("SAVEPOINT"), SELECT("SELECT"), SET("SET"), TABLE("TABLE"), TEMP("TEMP"), TEMPORARY("TEMPORARY"),
	THEN("THEN"), TO("TO"), TRANSACTION("TRANSACTION"), TRIGGER("TRIGGER"), UNION("UNION"), UNIQUE("UNIQUE"),
	UPDATE("UPDATE"), USING("USING"), VACUUM("VACUUM"), VALUES("VALUES"), VIEW("VIEW"), VIRTUAL("VIRTUAL"),
	WHEN("WHEN"), WHERE("WHERE"), WITH("WITH"), WITHOUT("WITHOUT"), VOID(null);

	/**
	 * The operator symbol
	 */
	public final String name;

	/**
	 * A dummy token for this keyword
	 */
	public final Token.Keyword token;

	Keyword(String name) {
		this.name = name;
		this.token = Token.Keyword.dummyFor(this);
	}

	@Override
	public void toSQL(Builder sql) {
		sql.append(this);
	}

	@Override
	public Token.Keyword token() { return token; }

	/**
	 * The list of every defined keywords
	 */
	public static final Map<String, Keyword> keywords;

	/**
	 * Initialize static structure
	 */
	static {
		Map<String, Keyword> kws = new HashMap<>();

		for (Keyword keyword : values()) {
			if (keyword == VOID) continue;
			String name = keyword.name;

			if (kws.containsKey(name)) {
				throw new IllegalStateException("An instance of " + name + " already exists");
			} else {
				kws.put(name, keyword);
			}
		}

		keywords = Collections.unmodifiableMap(kws);
	}

	/**
	 * Returns the Keyword matching the given name
	 */
	public static Optional<Keyword> from(String symbol) {
		return Optional.ofNullable(keywords.get(symbol.toUpperCase()));
	}
}
