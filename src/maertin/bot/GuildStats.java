package maertin.bot;

import java.util.ArrayList;

import net.dv8tion.jda.api.entities.Guild;

@SuppressWarnings("serial")
public class GuildStats extends ArrayList<Integer> {
	
	private long guildID;
	
	public GuildStats(long id) {
		this.guildID = id;
	}
	
	public long getID() {
		return guildID;
	}

	@Override
	//SCUFFED EQUALS METHOD
	/**
	 * Yes, I know this is probably a scuffed way of doing things.
	 * I do it so I can use ArrayList.contains on the main list.
	 */
	public boolean equals(Object o) {
		long compareID;
		if (o instanceof Guild)
			compareID = ((Guild) o).getIdLong();
		else if (o instanceof GuildStats)
			compareID = ((GuildStats) o).getID();
		else
			return false;
		return this.getID() == compareID;
	}
}
