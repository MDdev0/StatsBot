package maertin.dataFetchers;

import java.io.IOException;

import maertin.bot.StatPinger;

import org.jsoup.Jsoup;

/**
 * Object containing methods needed to track features of a Twitter User's page.
 * @author maertin - Discord: MDguy1547#8643
 */
public class TwitterUser {
	private final String URL = "https://mixerno.space/api/twitter-user-counter/user/";
	private final String NAME_LOCATION = "\"value\":\"name\",\"count\":\"";
	private final String ICON_LOCATION = "\"value\":\"pfp\",\"count\":\"";
	private final String FOLLOWER_LOCATION = "\"value\":\"followers\",\"count\":";
	
	private String userHandle;
	private String userName;
	private String userIcon;
	private int followCount;
	
	/**
	 * Builds an instance of a TwitterUser.<br>
	 * Uses the Mixerno API to get the information needed.
	 * @param handle - String of the Twitter User's handle.
	 * @throws IOException in case of any error
	 */
	public TwitterUser(String handle) throws IOException {
		userHandle = handle;
		final String apiText = Jsoup.connect(URL + handle).ignoreContentType(true).timeout(StatPinger.QUERY_TIMEOUT).get().text().replaceAll("\\s", "");
		userName = apiText.substring(apiText.indexOf(NAME_LOCATION) + NAME_LOCATION.length());
		userName = userName.substring(0, userName.indexOf('"'));
		userIcon = apiText.substring(apiText.indexOf(ICON_LOCATION) + ICON_LOCATION.length());
		userIcon = userIcon.substring(0, userIcon.indexOf('"'));
		String followCountStr = apiText.substring(apiText.indexOf(FOLLOWER_LOCATION) + FOLLOWER_LOCATION.length());
		followCount = Integer.parseInt(followCountStr.substring(0, followCountStr.indexOf("}")));
	}
	
	/**
	 * @return String userHandle - The user's handle.
	 */
	public String getUserHandle() {
		return userHandle;
	}
	
	/**
	 * @return String userName - The user's name.
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * @return String userIcon - The user's profile picture URL.
	 */
	public String getUserIcon() {
		return userIcon;
	}
	
	/**
	 * @return int followCount - The user's number of followers.
	 */
	public int getFollowCount() {
		return followCount;
	}
}
