package sqlartan.core.ast.token;

/**
 * Interface of anything that can be tokenized.
 * <p>
 * This interface is used to accept multiple input types, as long
 * as this type is able to produce a token representation of itself.
 *
 * @param <T> the type of the returned token
 */
public interface Tokenizable<T extends Token> {
	/**
	 * Returns a token representation of the object.
	 */
	T token();
}

