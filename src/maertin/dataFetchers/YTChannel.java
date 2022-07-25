package maertin.dataFetchers;

import java.io.IOException;

import maertin.bot.StatPinger;

import org.jsoup.Jsoup;

/**
 * Object containing features of YTData needed to track information about a YouTube Channel.
 * Very helpful when trying to reduce the amount of network traffic needed while maintaining the functionality of the static methods in YTData.
 * @author maertin - Discord: MDguy1547#8643
 */
public class YTChannel {
	private final String URL = "https://youtube.googleapis.com/youtube/v3/channels?part=statistics&part=snippet&maxResults=1&key="
			+ StatPinger.YOUTUBE_API_KEY + "&id=";
	private final String NAME_LOCATION = "\"title\":\"";
	private final String ICON_LOCATION = "\"high\":{\"url\":\"";
	private final String SUBSCRIBER_LOCATION = "\"subscriberCount\":\"";
	
	private String channelID;
	private String channelName;
	private String channelIcon;
	private int subCount;
	
	/**
	 * Builds an instance of a YTChannel.<br>
	 * Uses the YouTube Data API v3 to get the information needed.
	 * @param channelID - String of YouTube Channel ID.
	 * THIS IS NOT ALWAYS THE CHANNEL URL!
	 * @see YTData YTData.getChannelID() to be sure of the ID.
	 * @throws IOException in case of any error
	 */
	public YTChannel(String channelID) throws IOException {
		this.channelID = channelID;
		// CONNECTION TIMES OUT AFTER SET TIME! See AlertManager for handling of timeouts. 
		String apiText = Jsoup.connect(URL + channelID).timeout(StatPinger.QUERY_TIMEOUT).get().text().replaceAll("\\s", "");
		channelName = apiText.substring(apiText.indexOf(NAME_LOCATION) + NAME_LOCATION.length());
		channelName = channelName.substring(0, channelName.indexOf('"'));
		channelIcon = apiText.substring(apiText.indexOf(ICON_LOCATION) + ICON_LOCATION.length());
		channelIcon = channelIcon.substring(0, channelIcon.indexOf('"'));
		String subCountStr = apiText.substring(apiText.indexOf(SUBSCRIBER_LOCATION) + SUBSCRIBER_LOCATION.length());
		subCount = Integer.parseInt(subCountStr.substring(0, subCountStr.indexOf('"')));
	}
	
	/**
	 * @return String channelID - The channel ID.
	 */
	public String getChannelID() {
		return channelID;
	}
	
	/**
	 * @return String channelName - The channel name.
	 */
	public String getChannelName() {
		return channelName;
	}
	
	/**
	 * @return String channelIcon - The URL to the channel icon.
	 */
	public String getChannelIcon() {
		return channelIcon;
	}
	
	/**
	 * @return int subCount - The channel's subscriber count.
	 */
	public int getSubCount() {
		return subCount;
	}
}
