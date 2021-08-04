package maertin.bot;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import maertin.dataFetchers.YTChannel;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

public class AlertManager {
	// TODO instance variables
	static Timer timer = new Timer();
	
	static TimerTask refreshAll = new TimerTask() {
		@Override
		public void run() {
			// Update status just for fun
			StatPinger.jda.getPresence().setPresence(OnlineStatus.ONLINE, 
					Activity.streaming("data from " + StatPinger.sources.size() + " sources!", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
			// Refresh all sources
			int allowedTimeouts = 3;
			for (int s = 0; s < StatPinger.sources.size(); s++) {
				try {
					StatPinger.sources.set(s, refreshSource(StatPinger.sources.get(s)));
				}
				// Handle web timeouts
				catch (SocketTimeoutException sto) {
					String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
					System.out.println("[" + timestamp + "] Timed out getting data for source " + StatPinger.sources.get(s).getID() + " of type "
							+ StatPinger.sources.get(s).getType());
					// Stop looping if timing out too much
					allowedTimeouts--;
					if (allowedTimeouts == 0) {
						System.out.println("[" + timestamp + "] Too many timeouts! Stopping the loop...");
						StatPinger.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, 
								Activity.listening("way too many web timeouts! Stopped on source "
						+ s + " of " + StatPinger.sources.size()));
						return;
					}
				}
				// Handle other errors
				catch (Exception e) {
					String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
					System.out.println("[" + timestamp + "] Unable to get data for source " + StatPinger.sources.get(s).getID() + " of type "
							+ StatPinger.sources.get(s).getType());
					e.printStackTrace();
				}
			}
			// Update status just for fun
			StatPinger.jda.getPresence().setPresence(OnlineStatus.ONLINE, 
					Activity.playing("with save data. " + StatPinger.sources.size() + " sources being saved!"));
			// Save all sources to disk
			StatPinger.saveAll();
			// Update status just for fun
			StatPinger.jda.getPresence().setPresence(OnlineStatus.IDLE, 
					Activity.watching(StatPinger.sources.size() + " sources."));
		}
	};
	
	AlertManager() {
		timer.scheduleAtFixedRate(refreshAll, 0, StatPinger.REFRESH_RATE);
	}
	
	public void end() {
		refreshAll.cancel();
		timer.cancel();
	}
	
	/**
	 * Refreshes sources based on their type.<p>
	 * Message sending is done in other methods called by this method.
	 * @param source - The source object to refresh and send messages for.
	 * @return updated source after message has been sent.
	 * @throws SocketTimeoutException - If the source is timing out, refreshAll should cancel and wait until later.
	 */
	private static StatSource refreshSource(StatSource source) throws SocketTimeoutException {
		try {
			switch (source.getType()) {
			
			// YouTube subscriber alert
			case YTSub:
				YTChannel channel = new YTChannel(source.getID());
				if (source.getPrevValue() != channel.getSubCount()) { // Compare to last change
					ytSubMessage(source, channel); // Send alert messages
					source.updatePrevValue(channel.getSubCount());
				}
				break;
			}
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException)
				throw new SocketTimeoutException(e.getMessage());
			else {
				String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
				System.out.println("[" + timestamp + "] Unable to get data for source " + source.getID() + " of type "
						+ source.getType());
				e.printStackTrace();
			}
		}
		
		return source;
	}
	
	/**
	 * Sends messages for YouTube Subscriber alerts.
	 * This was done to clean up the refreshSource method.
	 * @param source  - Source object to alert for
	 * @param current - Current subscriber count
	 */
	private static void ytSubMessage(StatSource source, YTChannel channel) {
		// Don't send a message if this has never been tracked before
		if (source.getPrevValue() == -1) return;
		
		// Build embed
		EmbedBuilder alert = new EmbedBuilder();
		alert.setTitle("YouTube Subscriber alert for: " + channel.getChannelName(),
				"https://www.youtube.com/channel/" + source.getID());
		alert.setThumbnail(channel.getChannelIcon());
		alert.addField("Old count", Integer.toString(source.getPrevValue()), true);
		alert.addField("New count", Integer.toString(channel.getSubCount()), true);
		alert.setColor(0xff0000);
		
		// Send message to all subscribed guilds
		for (Guild g : source) {
			alert.setFooter((source.size() == 1 ? "You were the only server to get this alert!" : 
				source.size()-1 + " other servers got this alert."), g.getIconUrl());
			try {
				g.getTextChannelsByName("stat-pings", true).get(0).sendTyping().queue();
				g.getTextChannelsByName("stat-pings", true).get(0).sendMessage(alert.build()).queue();
			} catch (IndexOutOfBoundsException e) {
				if (g.getSystemChannel() != null) {
					g.getSystemChannel().sendTyping().queue();
					g.getSystemChannel().sendMessage(alert.build()).queue();
				} else {
					g.getDefaultChannel().sendTyping().queue();
					g.getDefaultChannel().sendMessage(alert.build()).queue();
				}
			}
		}
		alert.clear();
	}
}
