import static org.junit.Assert.*;

import org.junit.Test;

public class CountTest {

	@Test
	public void test() {
		String body = "aedjawdoa d awdpa 3r23j2p3rj 22 3er2jr 2er2 r2oir 2 ri2 r23r2r j";
		String word = "patrik";
		int count;
		count = Extractor.countNumberOf(body, word);
		assertTrue(count == 0);
		
		body = "aedjapatrikwdoa d awdpatrikpa 3r23j2p3patrikrj 22 3er2jr 2er2 r2opatrikir 2 ri2 r23r2r j";
		count = Extractor.countNumberOf(body, word);
		assertTrue(count == 4);
	}

}
