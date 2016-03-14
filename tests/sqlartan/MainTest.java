package sqlartan;

import org.junit.Test;
import java.sql.SQLException;
import static org.junit.Assert.*;

public class MainTest {
	@Test
	public void sqliteVersionIsThreeEightEleven() throws SQLException {
		assertEquals(Main.sqliteVersion(), "3.8.11");
	}
}
