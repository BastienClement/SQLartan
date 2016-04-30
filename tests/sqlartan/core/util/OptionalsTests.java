package sqlartan.core.util;

import org.junit.Test;
import sqlartan.util.Optionals;
import java.util.Optional;
import static org.junit.Assert.*;

public class OptionalsTests {
	@Test
	public void firstPresentTest() {
		Optional<String> first = Optionals.firstPresent(
				Optional::empty,
				() -> Optional.of("b"),
				() -> Optional.of("c")
		);

		assertTrue(first.isPresent());
		assertEquals("b", first.get());
	}
}
