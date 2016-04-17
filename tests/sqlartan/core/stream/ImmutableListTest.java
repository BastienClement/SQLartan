package sqlartan.core.stream;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ImmutableListTest {
	@Test
	public void fromArrayDoCopy() {
		String[] values = new String[] { "a", "b", "c" };
		ImmutableList<String> list = ImmutableList.from(values);
		values[0] = "z";
		assertEquals("a", list.get(0));
	}

	@Test
	public void fromCollectionIsIndependant() {
		List<String> values = Arrays.asList("a", "b", "c");
		ImmutableList<String> list = ImmutableList.from(values);
		values.set(0, "z");
		assertEquals("a", list.get(0));
	}

	@Test
	public void fromWithMapperIsNotIndependant() {
		String[] values = new String[] { "a", "b", "c" };
		ImmutableList<String> list = ImmutableList.from(values, str -> str + str);
		assertEquals("aa", list.get(0));
		values[0] = "z";
		assertEquals("zz", list.get(0));
	}

	@Test
	public void viewDoesNotCreateIntermediateLists() {
		ImmutableList<String> list = ImmutableList.from("a", "b", "c");
		IterableStream<String> a = list.view().map(str -> str + str);
		assertFalse(a instanceof ImmutableList);
		IterableStream<String> b = a.map(str -> str + str);
		assertFalse(b instanceof ImmutableList);
		assertEquals(Arrays.asList("aa", "bb", "cc"), b.map(str -> str.substring(0, 2)).toList());
	}
}
