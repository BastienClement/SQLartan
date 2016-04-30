package sqlartan.core.ast.parser;

import sqlartan.core.ast.token.TokenSource;

public abstract class Parser<T> {
	abstract public T parse(TokenSource source);
}
