package sqlartan.core.stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import static org.junit.Assert.*;

public class IterableStreamTests {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private class InfiniteConsumptionException extends RuntimeException {}

	private Stream<Integer> testStream() {
		return Stream.iterate(0, i -> {
			if (i > 20) throw new InfiniteConsumptionException();
			return i + 1;
		});
	}

	@Test
	public void fromStreamIsNotReiterable() {
		IterableStream<Integer> a = IterableStream.from(testStream());
		assertFalse(a.isReiterable());
		assertEquals(Arrays.asList(0, 1, 2), a.limit(3).toList());
		exception.expect(IllegalStateException.class);
		a.limit(3).count();
	}

	@Test
	public void fromSupplierIsReiterable() {
		IterableStream<Integer> a = IterableStream.from(this::testStream);
		assertTrue(a.isReiterable());
		assertEquals(Arrays.asList(0, 1, 2), a.limit(3).toList());
		assertEquals(Arrays.asList(0, 1, 2), a.limit(3).toList());
	}

	@Test
	public void fromIterableIsReiterable() {
		IterableStream<Integer> a = IterableStream.from(() -> testStream().iterator());
		assertTrue(a.isReiterable());
		assertEquals(Arrays.asList(0, 1, 2), a.limit(3).toList());
		assertEquals(Arrays.asList(0, 1, 2, 3), a.limit(4).toList());
	}

	@Test
	public void nonReiterableCanBeMadeReiterable() {
		IterableStream<Integer> a = IterableStream.from(testStream().limit(3));
		assertFalse(a.isReiterable());
		IterableStream<Integer> b = a.reiterable();
		assertTrue(b.isReiterable());
		assertEquals(b.toList(), b.toList());
	}

	@Test
	public void tranformOperationsMakeNonReiterable() {
		IterableStream<Integer> a = IterableStream.from(this::testStream);
		assertTrue(a.isReiterable());
		IterableStream<Integer> b = a.map(i -> i * 2);
		assertFalse(b.isReiterable());
		IterableStream<Integer> c = b.limit(5).reiterable();
		assertTrue(c.isReiterable());
	}

	@Test
	public void reiterableIsEager() {
		IterableStream<Integer> a = IterableStream.from(testStream());
		exception.expect(InfiniteConsumptionException.class);
		IterableStream<Integer> b = a.reiterable();
	}

	@Test
	public void streamAreIterable() {
		IterableStream<Integer> a = IterableStream.from(testStream());
		int i = 0;
		for (int j : a) {
			assertEquals(i++, j);
			if (i > 5) break;
		}
	}

	@Test
	public void customOperationsAreWorkingAsIntended() {
		IterableStream<Integer> a = IterableStream.from(this::testStream);

		// 1 * 2 * 3 * 4
		long reduced = a.skip(1).limit(4).reduce(1L, (acc, i) -> acc * i);
		assertEquals(24, reduced);

		IterableStream<Double> halfOdds = a.mapOptional(i -> Optional.ofNullable(i % 2 == 1 ? i / 2.0 : null));
		assertEquals(Arrays.asList(0.5, 1.5, 2.5), halfOdds.limit(3).toList());

		assertTrue(a.mapFirstOptional(i -> i / 2.0).isPresent());
		assertFalse(a.limit(0).mapFirstOptional(i -> i / 2.0).isPresent());

		long firstMapped = a.skip(3).mapFirst(i -> i * 5L);
		assertEquals(15L, firstMapped);

		exception.expect(NoSuchElementException.class);
		a.limit(0).mapFirst(i -> i * 5L);
	}
}
