package sqlartan.core.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static sqlartan.util.Matching.dispatch;
import static sqlartan.util.Matching.match;

public class MatchingTests {
	private abstract class Animal {}

	private abstract class Name {
		abstract String get();
	}

	private class Cat extends Animal {
		private class CatName extends Name {
			String get() { return "Tom"; }
		}
		private CatName catName() {
			return new CatName();
		}
	}

	private class Mouse extends Animal {
		private class MouseName extends Name {
			String get() { return "Jerry"; }
		}
		private MouseName mouseName() {
			return new MouseName();
		}
	}

	@Test
	public void matchingTests() {
		Name name = match((Animal) new Mouse(), Name.class)
				.when(Cat.class, Cat::catName)
				.when(Mouse.class, Mouse::mouseName)
				.orElse(() -> new Name() {
					String get() { return "Tweety"; }
				});
		assertEquals("Jerry", name.get());

		String strName = match((Animal) new Cat())
				.when(Cat.class, cat -> cat.catName().get())
				.when(Mouse.class, mouse -> mouse.mouseName().get())
				.orElseThrow(IllegalStateException::new);
		assertEquals("Tom", strName);

		final String[] matchedCase = { "???" };
		dispatch(new Object())
				.when(Cat.class, cat -> matchedCase[0] = "cat")
				.when(Mouse.class, cat -> matchedCase[0] = "mouse")
				.orElse(() -> matchedCase[0] = "default");
		assertEquals("default", matchedCase[0]);
	}
}
