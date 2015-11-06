package io.reflection.salesdatagather.model.enums;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ITunesPlatformTest {

	@Test
	public void test() {
		assertTrue(ITunesPlatform.IPOD==ITunesPlatform.getPlatformByName("iPod touch"));
		assertTrue(ITunesPlatform.IPHONE==ITunesPlatform.getPlatformByName("iPhone"));
		assertTrue(ITunesPlatform.IPAD==ITunesPlatform.getPlatformByName("iPad"));
		assertTrue(ITunesPlatform.DESKTOP==ITunesPlatform.getPlatformByName("Desktop"));
	}
}
