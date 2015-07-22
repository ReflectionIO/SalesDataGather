package io.reflection.salesdatagather.tasks;

import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.services.GatherTaskService;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.taskqueue.model.Task;

public class GatherTask implements Runnable{
	private transient static final Logger LOG = LoggerFactory.getLogger(GatherTask.class.getName());

	private final DataAccount dataAccount;
	private final Date dateToGatherFrom;
	private final Date dateToGatherTo;
	private final String itemIds;
	private final String countryCodeToGatherFor;
	private final GatherTaskService	gatherTaskService;
	private final String	mainItemId;
	private Task leasedTask;

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

	public Task getLeasedTask() {
		return leasedTask;
	}

	public void setLeasedTask(Task leasedTask) {
		this.leasedTask = leasedTask;
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
}
