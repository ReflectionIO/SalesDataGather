package io.reflection.salesdatagather.selenium;

import io.reflection.salesdatagather.AppConfig;
import io.reflection.salesdatagather.model.enums.ITunesLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ITunesHelper {
	public static final String	DOWNLOAD_LINK					= "download-csv";
	public static final String	SALES_LINK						= "sales-info";
	public static final String	SHARE_LINK						= "cicon-share";
	public static final String	LOCAL_TIMEZONE_LINK		= "local";
	public static final String	TIMEZONE_TOGGLE_LINK	= "utc-toggle";

	@Autowired
	private AppConfig appConfig;

	public String getDownloadUrl(Date dateToGatherFrom, Date dateToGatherTo, String itemIds, String countryCodeToGatherFor) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		ITunesLocation location = ITunesLocation.valueOf(countryCodeToGatherFor.toUpperCase());

		String url = "https://reportingitc2.apple.com/?startDate=" + sdf.format(dateToGatherFrom) + "&endDate=" + sdf.format(dateToGatherTo) + "&group=platform&filter_content=" + itemIds
				+ "&filter_piano_location=" + location.getItunesStoreFrontId(); // + "&filter_platform=iPad,iPhone,iPod,Windows%252C%2520Macintosh%252C%2520UNKNOWN"; Don't need to add the platform as we are adding all of them to the filter anyway

		return url;
	}

	public Path createTempDownloadsDir(Integer dataAccountId, String mainItemId, String countryCode, Date gatherFrom, Date gatherTo) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String from = sdf.format(gatherFrom);
		String to = sdf.format(gatherTo);

		return Files.createTempDirectory(Paths.get(appConfig.getTempDownloadDir()), ""+dataAccountId+"-"+mainItemId+"-"+countryCode+"-"+from+"-"+to+"-");
	}
}
