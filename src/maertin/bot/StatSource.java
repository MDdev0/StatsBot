package maertin.bot;

import java.io.File;
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
public class StatSource extends ArrayList<Guild> {
	
	/**
	 * Used with DataVersion for serialize and deserialize
	 */
	private static final long serialVersionUID = 1L;

	public enum SourceType {
		YTSub, // YouTube Subscribers
		TweetFollow // Twitter Followers
	}
	
	// Instance Variables
	private String sourceID;
	private SourceType sourceType;
	
	// -1 is the starting value, this will be changed the first time the source is checked.
	private int previousValue = -1;
	
	/**
	 * @param id - The ID of the source to follow (YouTube Channel ID, Twitter handle, etc.)
	 * @param type - The type of source this represents. 
	 */
	public StatSource(String id, SourceType type) {
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
	public SourceType getType() {
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
	 * @throws IOException if the supplied file could not be found or if there was an error updating the file
	 */
	public void deserialize(File file) throws UnsupportedOperationException, IOException {
		if (this.sourceID != null && this.sourceType != null) 
			throw new UnsupportedOperationException("This instance of StatSouce already contains "
					+ "complete data and should not be deserialized to!");
		Scanner input = new Scanner(file);
		
		// Check Data Version
		String dataVersion = input.nextLine();
		if (dataVersion.contains("DataVersion: "))
			dataVersion = dataVersion.substring(dataVersion.indexOf("DataVersion: ") + ("DataVersion: ".length()));
		else
			dataVersion = "-1"; // Impossible data version, always will try to be converted
		if (Long.parseLong(dataVersion) != serialVersionUID) {
			input.close(); // Discard old scanner.
			try {
				updateFile(file); // Update file
			} catch (Exception e) {
				throw new IOException(e);
			}
			input = new Scanner(file); // Reinitialize scanner
			// Method continues as normal with updated file
			input.nextLine(); // Bypass data version if this happens
		}
		
		this.sourceID = input.nextLine();
		this.sourceType = SourceType.valueOf(input.nextLine().trim());
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
	}
	
	/**
	 * Takes this StatSource object and serializes it to a file for recover after restarts.
	 * @param file - The file to write to
	 * @throws IOException if an error occurs while writing the file
	 */
	public void serialize(File file) throws IOException {
		FileWriter output = new FileWriter(file);
		output.append("DataVersion: " + serialVersionUID + "\n");
		output.append(sourceID + "\n");
		output.append(sourceType + "\n");
		output.append(previousValue + "\n");
		if (!this.isEmpty())
			for (Guild g : this) {
				output.append(g.getId());	
			}
		output.close();
	}
	
	/**
	 * Updates the provided file to the latest version of the save file format.<br>
	 * This will only be updated if the SerialVersionUID changes.<p>
	 * @param toUpdate - The file that needs to be updated.
	 * @throws IOException If something goes wrong
	 */
	// TODO: MAKE SURE TO UPDATE THIS WHEN THE SERIAL VERSION CHANGES!!
	public static void updateFile(File toUpdate) throws IOException {
		System.out.println("Updating outdated file: " + toUpdate.getPath());
		Scanner scan = new Scanner(toUpdate);
		ArrayList<String> lines = new ArrayList<String>();
		while (scan.hasNextLine())
			lines.add(scan.nextLine());
		scan.close();
		FileWriter rewrite = new FileWriter(toUpdate);
		
		try {
			// Check for each version of the file
			if (!lines.get(0).contains("DataVersion: ")) { // Version 0, before data versions implemented
				rewrite.append("DataVersion: " + serialVersionUID + "\n");
				rewrite.append(lines.get(0) + "\n");
				rewrite.append(SourceType.values()[Integer.parseInt(lines.get(1)) - 1].toString() + "\n");
				for (int i = 2; i < lines.size(); i++)
					rewrite.append(lines.get(i) + "\n");
			}
		} catch (Exception ex) {
			for (String line : lines)
				rewrite.append(line);
			throw ex;
		} finally {
			rewrite.close();
		}
	}
}
