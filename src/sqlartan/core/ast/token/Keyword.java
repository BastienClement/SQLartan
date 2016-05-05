package sqlartan.core.ast.token;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL Keyword
 */
public class Keyword extends Token<String> {
	/**
	 * List of defined Keyword tokens
	 */
	private static Map<String, Keyword> keywords = new HashMap<>();

	/**
	 * Token initialization
	 *
	 * @param name   the keyword name
	 * @param source the token source
	 * @param offset the token offset in the source
	 */
	private Keyword(String name, String source, int offset) {
		super(TokenType.KEYWORD, source, offset, name);
	}

	/**
	 * Token default initialization
	 * This method will register the token in the keywords map.
	 *
	 * @param name the token name
	 */
	private Keyword(String name) {
		this(name, "", -1);
		if (keywords.containsKey(name)) {
			throw new IllegalStateException("An instance of " + name + " already exists");
		}
		keywords.put(name, this);
	}

	/**
	 * Constructs a Keyword token.
	 * If the requested Keyword does not exists, returns null.
	 *
	 * @param keyword the keyword name
	 * @param source  the source code
	 * @param offset  the offset of the token in the source code
	 */
	public static Keyword from(String keyword, String source, int offset) {
		Keyword ref = keywords.get(keyword.toUpperCase());
		return ref != null ? new Keyword(ref.value, source, offset) : null;
	}

	/**
	 * Constructs a new instance of the same keyword.
	 *
	 * @param source the keyword source
	 * @param offset the offset of the token in the source code
	 */
	public Keyword at(String source, int offset) {
		return new Keyword(value, source, offset);
	}

	public static final Keyword ABORT = new Keyword("ABORT");
	public static final Keyword ACTION = new Keyword("ACTION");
	public static final Keyword ADD = new Keyword("ADD");
	public static final Keyword AFTER = new Keyword("AFTER");
	public static final Keyword ALL = new Keyword("ALL");
	public static final Keyword ALTER = new Keyword("ALTER");
	public static final Keyword ANALYZE = new Keyword("ANALYZE");
	public static final Keyword AND = new Keyword("AND");
	public static final Keyword AS = new Keyword("AS");
	public static final Keyword ASC = new Keyword("ASC");
	public static final Keyword ATTACH = new Keyword("ATTACH");
	public static final Keyword AUTOINCREMENT = new Keyword("AUTOINCREMENT");
	public static final Keyword BEFORE = new Keyword("BEFORE");
	public static final Keyword BEGIN = new Keyword("BEGIN");
	public static final Keyword BETWEEN = new Keyword("BETWEEN");
	public static final Keyword BY = new Keyword("BY");
	public static final Keyword CASCADE = new Keyword("CASCADE");
	public static final Keyword CASE = new Keyword("CASE");
	public static final Keyword CAST = new Keyword("CAST");
	public static final Keyword CHECK = new Keyword("CHECK");
	public static final Keyword COLLATE = new Keyword("COLLATE");
	public static final Keyword COLUMN = new Keyword("COLUMN");
	public static final Keyword COMMIT = new Keyword("COMMIT");
	public static final Keyword CONFLICT = new Keyword("CONFLICT");
	public static final Keyword CONSTRAINT = new Keyword("CONSTRAINT");
	public static final Keyword CREATE = new Keyword("CREATE");
	public static final Keyword CROSS = new Keyword("CROSS");
	public static final Keyword CURRENT_DATE = new Keyword("CURRENT_DATE");
	public static final Keyword CURRENT_TIME = new Keyword("CURRENT_TIME");
	public static final Keyword CURRENT_TIMESTAMP = new Keyword("CURRENT_TIMESTAMP");
	public static final Keyword DATABASE = new Keyword("DATABASE");
	public static final Keyword DEFAULT = new Keyword("DEFAULT");
	public static final Keyword DEFERRABLE = new Keyword("DEFERRABLE");
	public static final Keyword DEFERRED = new Keyword("DEFERRED");
	public static final Keyword DELETE = new Keyword("DELETE");
	public static final Keyword DESC = new Keyword("DESC");
	public static final Keyword DETACH = new Keyword("DETACH");
	public static final Keyword DISTINCT = new Keyword("DISTINCT");
	public static final Keyword DROP = new Keyword("DROP");
	public static final Keyword EACH = new Keyword("EACH");
	public static final Keyword ELSE = new Keyword("ELSE");
	public static final Keyword END = new Keyword("END");
	public static final Keyword ESCAPE = new Keyword("ESCAPE");
	public static final Keyword EXCEPT = new Keyword("EXCEPT");
	public static final Keyword EXCLUSIVE = new Keyword("EXCLUSIVE");
	public static final Keyword EXISTS = new Keyword("EXISTS");
	public static final Keyword EXPLAIN = new Keyword("EXPLAIN");
	public static final Keyword FAIL = new Keyword("FAIL");
	public static final Keyword FOR = new Keyword("FOR");
	public static final Keyword FOREIGN = new Keyword("FOREIGN");
	public static final Keyword FROM = new Keyword("FROM");
	public static final Keyword FULL = new Keyword("FULL");
	public static final Keyword GLOB = new Keyword("GLOB");
	public static final Keyword GROUP = new Keyword("GROUP");
	public static final Keyword HAVING = new Keyword("HAVING");
	public static final Keyword IF = new Keyword("IF");
	public static final Keyword IGNORE = new Keyword("IGNORE");
	public static final Keyword IMMEDIATE = new Keyword("IMMEDIATE");
	public static final Keyword IN = new Keyword("IN");
	public static final Keyword INDEX = new Keyword("INDEX");
	public static final Keyword INDEXED = new Keyword("INDEXED");
	public static final Keyword INITIALLY = new Keyword("INITIALLY");
	public static final Keyword INNER = new Keyword("INNER");
	public static final Keyword INSERT = new Keyword("INSERT");
	public static final Keyword INSTEAD = new Keyword("INSTEAD");
	public static final Keyword INTERSECT = new Keyword("INTERSECT");
	public static final Keyword INTO = new Keyword("INTO");
	public static final Keyword IS = new Keyword("IS");
	public static final Keyword IS_NOT = new Keyword("IS NOT");
	public static final Keyword ISNULL = new Keyword("ISNULL");
	public static final Keyword JOIN = new Keyword("JOIN");
	public static final Keyword KEY = new Keyword("KEY");
	public static final Keyword LEFT = new Keyword("LEFT");
	public static final Keyword LIKE = new Keyword("LIKE");
	public static final Keyword LIMIT = new Keyword("LIMIT");
	public static final Keyword MATCH = new Keyword("MATCH");
	public static final Keyword NATURAL = new Keyword("NATURAL");
	public static final Keyword NO = new Keyword("NO");
	public static final Keyword NOT = new Keyword("NOT");
	public static final Keyword NOTNULL = new Keyword("NOTNULL");
	public static final Keyword NULL = new Keyword("NULL");
	public static final Keyword OF = new Keyword("OF");
	public static final Keyword OFFSET = new Keyword("OFFSET");
	public static final Keyword ON = new Keyword("ON");
	public static final Keyword OR = new Keyword("OR");
	public static final Keyword ORDER = new Keyword("ORDER");
	public static final Keyword OUTER = new Keyword("OUTER");
	public static final Keyword PLAN = new Keyword("PLAN");
	public static final Keyword PRAGMA = new Keyword("PRAGMA");
	public static final Keyword PRIMARY = new Keyword("PRIMARY");
	public static final Keyword QUERY = new Keyword("QUERY");
	public static final Keyword RAISE = new Keyword("RAISE");
	public static final Keyword RECURSIVE = new Keyword("RECURSIVE");
	public static final Keyword REFERENCES = new Keyword("REFERENCES");
	public static final Keyword REGEXP = new Keyword("REGEXP");
	public static final Keyword REINDEX = new Keyword("REINDEX");
	public static final Keyword RELEASE = new Keyword("RELEASE");
	public static final Keyword RENAME = new Keyword("RENAME");
	public static final Keyword REPLACE = new Keyword("REPLACE");
	public static final Keyword RESTRICT = new Keyword("RESTRICT");
	public static final Keyword RIGHT = new Keyword("RIGHT");
	public static final Keyword ROLLBACK = new Keyword("ROLLBACK");
	public static final Keyword ROW = new Keyword("ROW");
	public static final Keyword ROWID = new Keyword("ROWID");
	public static final Keyword SAVEPOINT = new Keyword("SAVEPOINT");
	public static final Keyword SELECT = new Keyword("SELECT");
	public static final Keyword SET = new Keyword("SET");
	public static final Keyword TABLE = new Keyword("TABLE");
	public static final Keyword TEMP = new Keyword("TEMP");
	public static final Keyword TEMPORARY = new Keyword("TEMPORARY");
	public static final Keyword THEN = new Keyword("THEN");
	public static final Keyword TO = new Keyword("TO");
	public static final Keyword TRANSACTION = new Keyword("TRANSACTION");
	public static final Keyword TRIGGER = new Keyword("TRIGGER");
	public static final Keyword UNION = new Keyword("UNION");
	public static final Keyword UNIQUE = new Keyword("UNIQUE");
	public static final Keyword UPDATE = new Keyword("UPDATE");
	public static final Keyword USING = new Keyword("USING");
	public static final Keyword VACUUM = new Keyword("VACUUM");
	public static final Keyword VALUES = new Keyword("VALUES");
	public static final Keyword VIEW = new Keyword("VIEW");
	public static final Keyword VIRTUAL = new Keyword("VIRTUAL");
	public static final Keyword WHEN = new Keyword("WHEN");
	public static final Keyword WHERE = new Keyword("WHERE");
	public static final Keyword WITH = new Keyword("WITH");
	public static final Keyword WITHOUT = new Keyword("WITHOUT");
}
