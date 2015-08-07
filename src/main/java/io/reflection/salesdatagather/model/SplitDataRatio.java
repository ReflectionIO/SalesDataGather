package io.reflection.salesdatagather.model;

import java.util.Date;

public class SplitDataRatio {
	private Integer	dataAccountId;
	private Date		date;
	private String	country;
	private String	itemId;
	private Double	phoneRevenueRatio;
	private Double	tabletRevenueRatio;
	private Double	phoneIapRevenueRatio;
	private Double	tabletIapRevenueRatio;
	private Integer	phoneDownloads;
	private Integer	tabletDownloads;
	private Integer	totalDownloads;

	public Integer getDataAccountId() {
		return dataAccountId;
	}

	public void setDataAccountId(Integer dataAccountId) {
		this.dataAccountId = dataAccountId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public Double getPhoneRevenueRatio() {
		return phoneRevenueRatio;
	}

	public void setPhoneRevenueRatio(Double phoneRevenueRatio) {
		this.phoneRevenueRatio = phoneRevenueRatio;
	}

	public Double getTabletRevenueRatio() {
		return tabletRevenueRatio;
	}

	public void setTabletRevenueRatio(Double tabletRevenueRatio) {
		this.tabletRevenueRatio = tabletRevenueRatio;
	}

	public Double getPhoneIapRevenueRatio() {
		return phoneIapRevenueRatio;
	}

	public void setPhoneIapRevenueRatio(Double phoneIapRevenueRatio) {
		this.phoneIapRevenueRatio = phoneIapRevenueRatio;
	}

	public Double getTabletIapRevenueRatio() {
		return tabletIapRevenueRatio;
	}

	public void setTabletIapRevenueRatio(Double tabletIapRevenueRatio) {
		this.tabletIapRevenueRatio = tabletIapRevenueRatio;
	}

	public Integer getPhoneDownloads() {
		return phoneDownloads;
	}

	public void setPhoneDownloads(Integer phoneDownloads) {
		this.phoneDownloads = phoneDownloads;
	}

	public Integer getTabletDownloads() {
		return tabletDownloads;
	}

	public void setTabletDownloads(Integer tabletDownloads) {
		this.tabletDownloads = tabletDownloads;
	}

	public Integer getTotalDownloads() {
		return totalDownloads;
	}

	public void setTotalDownloads(Integer totalDownloads) {
		this.totalDownloads = totalDownloads;
	}
}
