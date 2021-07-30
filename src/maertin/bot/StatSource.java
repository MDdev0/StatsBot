package maertin.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Container for one statistic source's info <b>and</b> the Guilds listening to it.
 * @author maertin
 */
@SuppressWarnings("serial")
public class StatSource extends ArrayList<Guild> {

	// These are all the supported sources. 
	public static final int YOUTUBE_SUBSCRIBER = 1;
	
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
		sourceID = id;
		sourceType = type;
	}
	
	public StatSource() {}
	
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
	//SCUFFED EQUALS METHOD
	/**
	 * Yes, I know this is probably a scuffed way of doing things.
	 * I do it so I can use ArrayList.contains on the main list.
	 */
	public boolean equals(Object o) {
		return equalsIgnoreGuilds(o);
	}
	
	/*
	 * SCUFFED SERIALIZATION STUFF
	 * All of this code is probably TERRIBLE, but I have
	 * no clue how to do serialization properly in this scenario.
	 * Too bad, I guess.
	 */
	
	/**
	 * Takes serialized data from a file and places it into a new StatSource.
	 * @param file - The file to read from
	 * @throws UnsupportedOperationException if this instance has already contains data
	 * @throws FileNotFoundException if the supplied file could not be found
	 */
	public void deserialize(File file) throws UnsupportedOperationException, FileNotFoundException {
		if (this.sourceID != null && this.sourceType != 0) 
			throw new UnsupportedOperationException("This instance of StatSouce already contains "
					+ "complete data and should not be deserialized to!");
		Scanner input = new Scanner(file);
		this.sourceID = input.nextLine();
		this.sourceType = Integer.parseInt(input.nextLine());
		this.previousValue = Integer.parseInt(input.nextLine());
		while (input.hasNextLine()) {
			String guildID = input.nextLine();
			try {
				this.add(StatPinger.jda.getGuildById(guildID));
				
			} catch (NumberFormatException nfe) {
				String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
				System.out.println("[" + timestamp + "] Error importing a guild to this source: " + sourceID + " of type " + sourceType);
				System.out.println(nfe.getMessage());
				System.out.println("Supplied Guild ID: " + guildID);
			}
		}
		input.close();
		
		// Debug code
//		System.out.println("Finished importing this source: " + sourceID + " of type " + sourceType);
//		System.out.println(this.size() + " guilds.");
//		for (Guild g : this) {
//			System.out.println(g.getName());
//		}
	}
	
	/**
	 * Takes this StatSource object and serializes it to a file for recover after restarts.
	 * @param file - The file to write to
	 * @throws IOException if an error occurs while writing the file
	 */
	@SuppressWarnings("resource") // output does get closed.
	public void serialize(File file) throws IOException {
		FileWriter output = new FileWriter(file);
		output.append(sourceID + "\n");
		output.append(sourceType + "\n");
		output.append(previousValue + "\n");
		if (!this.isEmpty())
			for (Guild g : this) {
				output.append(g.getId());	
			}
		output.close();
	}
}
