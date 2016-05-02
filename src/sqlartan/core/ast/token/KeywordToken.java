package sqlartan.core.ast.token;

import java.util.HashMap;
import java.util.Map;

public class KeywordToken extends Token<String> {
	private static Map<String, KeywordToken> keywords = new HashMap<>();

	private KeywordToken(String name, String source, int offset) {
		super(TokenType.KEYWORD, source, offset, name);
	}

	private KeywordToken(String name) {
		this(name, "", -1);
		if (keywords.containsKey(name)) {
			throw new IllegalStateException("An instance of " + name + " already exists");
		}
		keywords.put(name, this);
	}

	public static KeywordToken from(String keyword, String source, int offset) {
		KeywordToken ref = keywords.get(keyword.toUpperCase());
		return ref != null ? new KeywordToken(ref.value, source, offset) : null;
	}

	public static final KeywordToken ABORT = new KeywordToken("ABORT");
	public static final KeywordToken ACTION = new KeywordToken("ACTION");
	public static final KeywordToken ADD = new KeywordToken("ADD");
	public static final KeywordToken AFTER = new KeywordToken("AFTER");
	public static final KeywordToken ALL = new KeywordToken("ALL");
	public static final KeywordToken ALTER = new KeywordToken("ALTER");
	public static final KeywordToken ANALYZE = new KeywordToken("ANALYZE");
	public static final KeywordToken AND = new KeywordToken("AND");
	public static final KeywordToken AS = new KeywordToken("AS");
	public static final KeywordToken ASC = new KeywordToken("ASC");
	public static final KeywordToken ATTACH = new KeywordToken("ATTACH");
	public static final KeywordToken AUTOINCREMENT = new KeywordToken("AUTOINCREMENT");
	public static final KeywordToken BEFORE = new KeywordToken("BEFORE");
	public static final KeywordToken BEGIN = new KeywordToken("BEGIN");
	public static final KeywordToken BETWEEN = new KeywordToken("BETWEEN");
	public static final KeywordToken BY = new KeywordToken("BY");
	public static final KeywordToken CASCADE = new KeywordToken("CASCADE");
	public static final KeywordToken CASE = new KeywordToken("CASE");
	public static final KeywordToken CAST = new KeywordToken("CAST");
	public static final KeywordToken CHECK = new KeywordToken("CHECK");
	public static final KeywordToken COLLATE = new KeywordToken("COLLATE");
	public static final KeywordToken COLUMN = new KeywordToken("COLUMN");
	public static final KeywordToken COMMIT = new KeywordToken("COMMIT");
	public static final KeywordToken CONFLICT = new KeywordToken("CONFLICT");
	public static final KeywordToken CONSTRAINT = new KeywordToken("CONSTRAINT");
	public static final KeywordToken CREATE = new KeywordToken("CREATE");
	public static final KeywordToken CROSS = new KeywordToken("CROSS");
	public static final KeywordToken CURRENT_DATE = new KeywordToken("CURRENT_DATE");
	public static final KeywordToken CURRENT_TIME = new KeywordToken("CURRENT_TIME");
	public static final KeywordToken CURRENT_TIMESTAMP = new KeywordToken("CURRENT_TIMESTAMP");
	public static final KeywordToken DATABASE = new KeywordToken("DATABASE");
	public static final KeywordToken DEFAULT = new KeywordToken("DEFAULT");
	public static final KeywordToken DEFERRABLE = new KeywordToken("DEFERRABLE");
	public static final KeywordToken DEFERRED = new KeywordToken("DEFERRED");
	public static final KeywordToken DELETE = new KeywordToken("DELETE");
	public static final KeywordToken DESC = new KeywordToken("DESC");
	public static final KeywordToken DETACH = new KeywordToken("DETACH");
	public static final KeywordToken DISTINCT = new KeywordToken("DISTINCT");
	public static final KeywordToken DROP = new KeywordToken("DROP");
	public static final KeywordToken EACH = new KeywordToken("EACH");
	public static final KeywordToken ELSE = new KeywordToken("ELSE");
	public static final KeywordToken END = new KeywordToken("END");
	public static final KeywordToken ESCAPE = new KeywordToken("ESCAPE");
	public static final KeywordToken EXCEPT = new KeywordToken("EXCEPT");
	public static final KeywordToken EXCLUSIVE = new KeywordToken("EXCLUSIVE");
	public static final KeywordToken EXISTS = new KeywordToken("EXISTS");
	public static final KeywordToken EXPLAIN = new KeywordToken("EXPLAIN");
	public static final KeywordToken FAIL = new KeywordToken("FAIL");
	public static final KeywordToken FOR = new KeywordToken("FOR");
	public static final KeywordToken FOREIGN = new KeywordToken("FOREIGN");
	public static final KeywordToken FROM = new KeywordToken("FROM");
	public static final KeywordToken FULL = new KeywordToken("FULL");
	public static final KeywordToken GLOB = new KeywordToken("GLOB");
	public static final KeywordToken GROUP = new KeywordToken("GROUP");
	public static final KeywordToken HAVING = new KeywordToken("HAVING");
	public static final KeywordToken IF = new KeywordToken("IF");
	public static final KeywordToken IGNORE = new KeywordToken("IGNORE");
	public static final KeywordToken IMMEDIATE = new KeywordToken("IMMEDIATE");
	public static final KeywordToken IN = new KeywordToken("IN");
	public static final KeywordToken INDEX = new KeywordToken("INDEX");
	public static final KeywordToken INDEXED = new KeywordToken("INDEXED");
	public static final KeywordToken INITIALLY = new KeywordToken("INITIALLY");
	public static final KeywordToken INNER = new KeywordToken("INNER");
	public static final KeywordToken INSERT = new KeywordToken("INSERT");
	public static final KeywordToken INSTEAD = new KeywordToken("INSTEAD");
	public static final KeywordToken INTERSECT = new KeywordToken("INTERSECT");
	public static final KeywordToken INTO = new KeywordToken("INTO");
	public static final KeywordToken IS = new KeywordToken("IS");
	public static final KeywordToken ISNULL = new KeywordToken("ISNULL");
	public static final KeywordToken JOIN = new KeywordToken("JOIN");
	public static final KeywordToken KEY = new KeywordToken("KEY");
	public static final KeywordToken LEFT = new KeywordToken("LEFT");
	public static final KeywordToken LIKE = new KeywordToken("LIKE");
	public static final KeywordToken LIMIT = new KeywordToken("LIMIT");
	public static final KeywordToken MATCH = new KeywordToken("MATCH");
	public static final KeywordToken NATURAL = new KeywordToken("NATURAL");
	public static final KeywordToken NO = new KeywordToken("NO");
	public static final KeywordToken NOT = new KeywordToken("NOT");
	public static final KeywordToken NOTNULL = new KeywordToken("NOTNULL");
	public static final KeywordToken NULL = new KeywordToken("NULL");
	public static final KeywordToken OF = new KeywordToken("OF");
	public static final KeywordToken OFFSET = new KeywordToken("OFFSET");
	public static final KeywordToken ON = new KeywordToken("ON");
	public static final KeywordToken OR = new KeywordToken("OR");
	public static final KeywordToken ORDER = new KeywordToken("ORDER");
	public static final KeywordToken OUTER = new KeywordToken("OUTER");
	public static final KeywordToken PLAN = new KeywordToken("PLAN");
	public static final KeywordToken PRAGMA = new KeywordToken("PRAGMA");
	public static final KeywordToken PRIMARY = new KeywordToken("PRIMARY");
	public static final KeywordToken QUERY = new KeywordToken("QUERY");
	public static final KeywordToken RAISE = new KeywordToken("RAISE");
	public static final KeywordToken RECURSIVE = new KeywordToken("RECURSIVE");
	public static final KeywordToken REFERENCES = new KeywordToken("REFERENCES");
	public static final KeywordToken REGEXP = new KeywordToken("REGEXP");
	public static final KeywordToken REINDEX = new KeywordToken("REINDEX");
	public static final KeywordToken RELEASE = new KeywordToken("RELEASE");
	public static final KeywordToken RENAME = new KeywordToken("RENAME");
	public static final KeywordToken REPLACE = new KeywordToken("REPLACE");
	public static final KeywordToken RESTRICT = new KeywordToken("RESTRICT");
	public static final KeywordToken RIGHT = new KeywordToken("RIGHT");
	public static final KeywordToken ROLLBACK = new KeywordToken("ROLLBACK");
	public static final KeywordToken ROW = new KeywordToken("ROW");
	public static final KeywordToken SAVEPOINT = new KeywordToken("SAVEPOINT");
	public static final KeywordToken SELECT = new KeywordToken("SELECT");
	public static final KeywordToken SET = new KeywordToken("SET");
	public static final KeywordToken TABLE = new KeywordToken("TABLE");
	public static final KeywordToken TEMP = new KeywordToken("TEMP");
	public static final KeywordToken TEMPORARY = new KeywordToken("TEMPORARY");
	public static final KeywordToken THEN = new KeywordToken("THEN");
	public static final KeywordToken TO = new KeywordToken("TO");
	public static final KeywordToken TRANSACTION = new KeywordToken("TRANSACTION");
	public static final KeywordToken TRIGGER = new KeywordToken("TRIGGER");
	public static final KeywordToken UNION = new KeywordToken("UNION");
	public static final KeywordToken UNIQUE = new KeywordToken("UNIQUE");
	public static final KeywordToken UPDATE = new KeywordToken("UPDATE");
	public static final KeywordToken USING = new KeywordToken("USING");
	public static final KeywordToken VACUUM = new KeywordToken("VACUUM");
	public static final KeywordToken VALUES = new KeywordToken("VALUES");
	public static final KeywordToken VIEW = new KeywordToken("VIEW");
	public static final KeywordToken VIRTUAL = new KeywordToken("VIRTUAL");
	public static final KeywordToken WHEN = new KeywordToken("WHEN");
	public static final KeywordToken WHERE = new KeywordToken("WHERE");
	public static final KeywordToken WITH = new KeywordToken("WITH");
	public static final KeywordToken WITHOUT = new KeywordToken("WITHOUT");
}
