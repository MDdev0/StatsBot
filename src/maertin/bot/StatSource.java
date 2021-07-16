package maertin.bot;

import java.util.ArrayList;

import net.dv8tion.jda.api.entities.Guild;

/**
 * @author maertin
 * Container for one statistic source's info <b>and</b> the Guilds listening to it.
 */
@SuppressWarnings("serial")
public class StatSource extends ArrayList<Guild> {

	// These are all the supported sources. 
	public static final int YOUTUBE_SUBSCRIBER = 0;
	
	// Instance Variables
	private String sourceID;
	private int sourceType;
	
	// -1 is the starting value, this will be changed the first time the source is checked.
	private int previousValue = -1;
	
	/**
	 * @param id - The ID of the source to follow (YouTube Channel ID, Twitter handle, etc.)
	 * @param type - The type of source this represents. 
	 */
	public StatSource(String id, int type) {
		super();
		sourceID = id;
		sourceType = type;
	}
	
	/**
	 * @return The source's ID as a String.
	 */
	public String getID() {
		return sourceID;
	}
	
	/**
	 * @return The source's Type as an int.
	 */
	public int getType() {
		return sourceType;
	}
	
	/**
	 * This is intended to be checked against the current value to see if it has changed enough to warrant an alert.
	 * @return The previous value to check against.
	 */
	public int getPrevValue() {
		return previousValue;
	}
	
	/**
	 * Use this when the current value has changed substantially enough from the previous value.<p>
	 * After an alert is sent, update the old value here.
	 * @param newValue - The int to set as the new previous value to check for changes.
	 */
	public void updatePrevValue(int newValue) {
		previousValue = newValue;
	}
	
	/**
	 * Determines if two instances of StatSource are equal <b>without looking at Guilds listening to the source.</b><p>
	 * Only compares the ID and the Type of the caller and supplied object.
	 * @param o - object to compare with caller
	 * @return true if:
	 * <ul>
	 * <li>o is the caller (self check)</li>
	 * OR
	 * <li>o's Type and ID are the same as the caller</li>
	 * </ul><p>
	 * false if:
	 * <ul>
	 * <li>o is null</li>
	 * OR
	 * <li>o's Type and ID are different from that of the caller</li>
	 * </ul>
	 */
	public boolean equalsIgnoreGuilds(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (this.getClass() != o.getClass()) return false;
		StatSource s = (StatSource) o;
		return (this.getType() == s.getType() && this.getID().equals(s.getID()));
	}
	
	@Override
	//SCUFFED
	/**
	 * Yes, I know this is probably a scuffed way of doing things.
	 * I do it so I can use ArrayList.contains on the main list.
	 */
	public boolean equals(Object o) {
		return equalsIgnoreGuilds(o);
	}
}
