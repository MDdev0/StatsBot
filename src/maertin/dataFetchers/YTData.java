package maertin.dataFetchers;

import java.io.IOException;

import maertin.bot.StatPinger;

import org.jsoup.Jsoup;

/**
 * Class with methods to get information about YouTube Channels.
 * Only channels are currently supported, more may be added later.
 * @author maertin - Discord: MDguy1547#8643
 */
public class YTData {
	
	/**
	 * Uses a provided YouTube Channel URL to pull the Channel ID from the channel's HTML code.
	 * @param url - Channel URL to get Channel ID from.
	 * @return Channel ID in the form of a String.
	 * @throws IOException in case of any error
	 */
	public static String getChannelID(String url) throws IOException {
		// CONNECTION TIMES OUT AFTER SET TIME! See AlertManager for handling of timeouts. 
		return Jsoup.connect(url).timeout(StatPinger.QUERY_TIMEOUT).get().getElementsByAttributeValue("itemprop", "channelId").attr("content");
	}
	
	/**
	 * Uses the Mixerno API to pull the provided Channel ID's subscriber count.
	 * @param channelID - String of YouTube Channel ID.
	 * THIS IS NOT ALWAYS THE CHANNEL URL!
	 * Use getChannelID to be sure of the ID.
	 * @return the Subscriber count as an int
	 * @throws IOException in case of any error
	 * 
	 * @deprecated Connects to the web for every request instead of storing them locally.<br><b>NOT UPDATED TO MIXERNO V6!</b>
	 * @see YTChannel
	 */
	public static int getSubCount(String channelID) throws IOException {
		String url = "https://mixerno.space/api/yt/channel/" + channelID;
		String fullText = Jsoup.connect(url).get().text();
		String shortened = fullText.substring(fullText.indexOf("\"subscriberCount\": \"") + 20);
		shortened = shortened.substring(0, shortened.indexOf('"'));
		return Integer.parseInt(shortened);
	}
	
	/**
	 * Uses the Mixerno API to get the provided Channel ID's name.
	 * @param channelID - String of YouTube Channel ID.
	 * THIS IS NOT ALWAYS THE CHANNEL URL!
	 * Use getChannelID to be sure of the ID.
	 * @return the channel's name
	 * @throws IOException in case of any error
	 * 
	 * @deprecated Connects to the web for every request instead of storing them locally.<br><b>NOT UPDATED TO MIXERNO V6!</b>
	 * @see YTChannel
	 */
	public static String getChannelName(String channelID) throws IOException {
		String url = "https://mixerno.space/api/yt/channel/" + channelID;
		String fullText = Jsoup.connect(url).get().text();
		String shortened = fullText.substring(fullText.indexOf("\"channel\": { \"title\": \"") + 23);
		shortened = shortened.substring(0, shortened.indexOf('"'));
		return shortened;
	}
	
	/**
	 * Uses the Mixerno API to get the provided Channel ID's icon URL.
	 * @param channelID - String of YouTube Channel ID.
	 * THIS IS NOT ALWAYS THE CHANNEL URL!
	 * Use getChannelID to be sure of the ID.
	 * @return the channel's icon URL
	 * @throws IOException in case of any error
	 * 
	 * @deprecated Connects to the web for every request instead of storing them locally.<br><b>NOT UPDATED TO MIXERNO V6!</b>
	 * @see YTChannel
	 */
	public static String getChannelIcon(String channelID) throws IOException {
		String url = "https://mixerno.space/api/yt/channel/" + channelID;
		String fullText = Jsoup.connect(url).get().text();
		String shortened = fullText.substring(fullText.indexOf("\"high\": { \"url\": \"") + 18);
		shortened = shortened.substring(0, shortened.indexOf('"'));
		return shortened;
	}
}
