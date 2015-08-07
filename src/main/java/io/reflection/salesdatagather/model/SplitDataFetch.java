package io.reflection.salesdatagather.model;

import java.sql.Timestamp;
import java.util.Date;

public class SplitDataFetch {
	private Integer		splitDataFetchId;
	private Integer		dataAccountId;
	private Date			fetchDate;
	private Timestamp	fetchTime;
	private Date			fromDate;
	private Date			toDate;
	private String		country;
	private String		itemId;
	private String		downloadsReportUrl;
	private String		salesReportUrl;
	private String		iapReportUrl;
	private String		status;

	public Integer getSplitDataFetchId() {
		return splitDataFetchId;
	}

	public void setSplitDataFetchId(Integer splitDataFetchId) {
		this.splitDataFetchId = splitDataFetchId;
	}

	public Integer getDataAccountId() {
		return dataAccountId;
	}

	public void setDataAccountId(Integer dataAccountId) {
		this.dataAccountId = dataAccountId;
	}

	public Date getFetchDate() {
		return fetchDate;
	}

	public void setFetchDate(Date fetchDate) {
		this.fetchDate = fetchDate;
	}

	public Timestamp getFetchTime() {
		return fetchTime;
	}

	public void setFetchTime(Timestamp fetchTime) {
		this.fetchTime = fetchTime;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDownloadsReportUrl() {
		return downloadsReportUrl;
	}

	public void setDownloadsReportUrl(String downloadsReportUrl) {
		this.downloadsReportUrl = downloadsReportUrl;
	}

	public String getSalesReportUrl() {
		return salesReportUrl;
	}

	public void setSalesReportUrl(String salesReportUrl) {
		this.salesReportUrl = salesReportUrl;
	}

	public String getIapReportUrl() {
		return iapReportUrl;
	}

	public void setIapReportUrl(String iapReportUrl) {
		this.iapReportUrl = iapReportUrl;
	}
}
