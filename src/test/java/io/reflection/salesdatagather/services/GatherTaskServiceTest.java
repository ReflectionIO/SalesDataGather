package io.reflection.salesdatagather.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.reflection.salesdatagather.BaseSpringTestClass;
import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.model.nondb.GatherTask;

public class GatherTaskServiceTest extends BaseSpringTestClass {

	@Autowired
	private GatherTaskService service;

	// @Ignore
	@Test
	public void executeGatherTaskTest() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String gatherFrom = "2015-06-01";
		String gatherTo = "2015-06-30";

		// MINICLIP
		// String mainItemId = "543186831"; // 8 Ball Pool
		// String iapItemIds =
		// "543186831,645476871,778734224,691912989,570688418,691914155,671762300,570687934,570689017,691914487,572549063,778735142,691913844,778730011,572548834,570687600,778730328,570689437,778735036,572548407,572548497,778732816,572548843,691914886,572547947,778730206,570689052,691916150,778734121,890397530,890397720,890401534,890404521,881896592,881904756,926752480,926752430,938246435";
		// String username = "finance@miniclip.com";
		// String clearPassword = "CHECK DB";

		// SPACEHOPPER
		String mainItemId = "690467808"; // Delta-V Racing Free;
		String iapItemIds = "690557436,690558493";
		String username = "SPACEHOPPERSTUDIOS";
		String clearPassword = "CHECK DB";

		DataAccount account = new DataAccount();
		account.setId(1);
		account.setUsername(username);
		account.setClearPassword(clearPassword);

		GatherTask task = new GatherTask(account, sdf.parse(gatherFrom), sdf.parse(gatherTo), iapItemIds, mainItemId, "gb", service);
		service.executeGather(task);
	}
}
