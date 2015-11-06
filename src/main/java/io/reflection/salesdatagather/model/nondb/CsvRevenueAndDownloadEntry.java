package io.reflection.salesdatagather.model.nondb;

import io.reflection.salesdatagather.model.enums.ITunesPlatform;

import java.util.Date;

public class CsvRevenueAndDownloadEntry {
	private Date date;

	//Platforms: iPhone, iPad, iPod, Desktop (Mac and Windows)
	private int[] downloadsByPlatform = new int[ITunesPlatform.values().length];
	private int[] revenueByPlatform = new int[ITunesPlatform.values().length];
	private int[] iapRevenueByPlatform = new int[ITunesPlatform.values().length];


	public CsvRevenueAndDownloadEntry(Date date) {
		this.date = date;

		for(int i=0;i<ITunesPlatform.values().length;i++) {
			downloadsByPlatform[i]=0;
			revenueByPlatform[i]=0;
			iapRevenueByPlatform[i]=0;
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int[] getDownloadsByPlatform() {
		return downloadsByPlatform;
	}

	public void setDownloadsByPlatform(int[] downloadsByPlatform) {
		this.downloadsByPlatform = downloadsByPlatform;
	}

	public int[] getRevenueByPlatform() {
		return revenueByPlatform;
	}

	public void setRevenueByPlatform(int[] revenueByPlatform) {
		this.revenueByPlatform = revenueByPlatform;
	}

	public int[] getIapRevenueByPlatform() {
		return iapRevenueByPlatform;
	}

	public void setIapRevenueByPlatform(int[] iapRevenueByPlatform) {
		this.iapRevenueByPlatform = iapRevenueByPlatform;
	}

	public void setDownloadValue(ITunesPlatform platform, int value) {
		downloadsByPlatform[platform.ordinal()] = value;
	}

	public void setRevenueValue(ITunesPlatform platform, int value) {
		revenueByPlatform[platform.ordinal()] = value;
	}

	public void setIapRevenueValue(ITunesPlatform platform, int value) {
		iapRevenueByPlatform[platform.ordinal()] = value;
	}

	public int getDownloadValue(ITunesPlatform platform) {
		return downloadsByPlatform[platform.ordinal()];
	}

	public int getRevenueValue(ITunesPlatform platform) {
		return revenueByPlatform[platform.ordinal()];
	}

	public int getIapRevenueValue(ITunesPlatform platform) {
		return iapRevenueByPlatform[platform.ordinal()];
	}
}
