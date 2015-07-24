package io.reflection.salesdatagather.model.nondb;

import java.util.Date;

import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.services.GatherTaskService;

public class GatherTask implements Runnable{
	private final DataAccount dataAccount;
	private final Date dateToGatherFrom;
	private final Date dateToGatherTo;
	private final String itemIds;
	private final String countryCodeToGatherFor;
	private final GatherTaskService	gatherTaskService;
	private final String	mainItemId;
	private LeasedTask leasedTask;

	public GatherTask(DataAccount dataAccount, Date dateToGatherFrom, Date dateToGatherTo, String itemIds, String mainItemId, String countryCodeToGatherFor, GatherTaskService gatherTaskService) {
		this.dataAccount = dataAccount;
		this.dateToGatherFrom = dateToGatherFrom;
		this.dateToGatherTo = dateToGatherTo;
		this.itemIds = itemIds;
		this.mainItemId = mainItemId;
		this.countryCodeToGatherFor = countryCodeToGatherFor;
		this.gatherTaskService = gatherTaskService;
	}

	@Override
	public void run() {
		gatherTaskService.executeGather(this);
	}

	public DataAccount getDataAccount() {
		return dataAccount;
	}

	public Date getDateToGatherFrom() {
		return dateToGatherFrom;
	}

	public Date getDateToGatherTo() {
		return dateToGatherTo;
	}

	public String getItemIds() {
		return itemIds;
	}

	public String getMainItemId() {
		return mainItemId;
	}

	public String getCountryCodeToGatherFor() {
		return countryCodeToGatherFor;
	}

	public LeasedTask getLeasedTask() {
		return leasedTask;
	}
}
