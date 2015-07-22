package io.reflection.salesdatagather.model.enums;

public enum ITunesPlatform {
	IPHONE("iPhone"),
	IPAD("iPad"),
	IPOD("iPod touch"),
	DESKTOP("Desktop"); //(Windows, Macintosh, UNKNOWN)

	private String	platformName;

	private ITunesPlatform(String platformName) {
		this.platformName = platformName;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public static ITunesPlatform getPlatformByName(String name) {
		for(ITunesPlatform platform: values()) {
			if(platform.platformName.equalsIgnoreCase(name)) return platform;
		}

		return null;
	}
}
