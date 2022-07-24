package maertin.bot;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import maertin.dataFetchers.TwitterUser;
import maertin.dataFetchers.YTChannel;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

public class AlertManager {
	
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
						+ (s+1) + " of " + StatPinger.sources.size()));
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
			
			// Twitter follower alerts
			// Will only be sent when one of the top three digits changes
			case TweetFollow:
				TwitterUser user = new TwitterUser(source.getID());
				
				// Truncate the current number to first three digits
				int truncCurr = user.getFollowCount();
				int zeros = 0;
				while (truncCurr > 1000) {
					truncCurr /= 10;
					zeros++;
				}
				// Append appropriate number of 0s
				truncCurr *= (int) Math.pow(10, zeros);
				
				// Truncate previous number to first three digits
				int truncPrev = source.getPrevValue();
				zeros = 0;
				// Get first 3 digits
				while (truncPrev > 1000) {
					truncPrev /= 10;
					zeros++;
				}
				// Append appropriate number of 0s
				truncPrev *= (int) Math.pow(10, zeros);
				
				// Truncate previous announced number to first three digits 
				int truncPrevAnnounced = source.getPrevAnnounced();
				zeros = 0;
				// Get first 3 digits
				while (truncPrevAnnounced > 1000) {
					truncPrevAnnounced /= 10;
					zeros++;
				}
				// Append AND SAVE appropriate number of 0s
				int announcedZeros = zeros;
				truncPrevAnnounced *= (int) Math.pow(10, announcedZeros);
				
				// Compare!
				if (truncCurr != truncPrev) {
					/*
					 * Value is only announced if one of the following conditions are met:
					 * - truncCurr > truncPrevAnnounced
					 * - truncCurr < truncPrevAnnounced - (1 of its least significant digit)
					 * This effectively stops alerts when bouncing between two values with only
					 * 	a one significant digit difference
					 */
					if (truncCurr > truncPrevAnnounced || truncCurr < (truncPrevAnnounced - Math.pow(10, zeros))) {
						tweetFollowMessage(source, user);
						source.updateAnnouncedVal(user.getFollowCount());
					}
					source.updatePrevValue(user.getFollowCount());
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
	 * Sends pre-built alert embeds to all of a sources guilds.<br>
	 * This process is the same regardless of the type of source.
	 * @param alert - Pre-Built Embed to send out.
	 * @param source - Source that contains the guilds that must be alerted
	 */
	private static void sendAlertMessage(EmbedBuilder alert, StatSource source) {
		// Send message to all subscribed guilds
		for (Guild g : source) {
			alert.setFooter((source.size() == 1 ? "You were the only server to get this alert!"
					: source.size() - 1 + " other servers got this alert."), g.getIconUrl());
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
	}
	
	/**
	 * Builds messages for YouTube Subscriber alerts.
	 * This was done to clean up the refreshSource method.
	 * @param source  - Source object to alert for
	 * @param channel - Channel object that contains information about the alert
	 */
	private static void ytSubMessage(StatSource source, YTChannel channel) {
		// Don't send a message if this has never been tracked before
		if (source.getPrevValue() == -1) return;
		
		// Build embed
		EmbedBuilder alert = new EmbedBuilder()
		.setTitle("YouTube Subscriber alert for: " + channel.getChannelName(),
				"https://www.youtube.com/channel/" + channel.getChannelID())
		.setThumbnail(channel.getChannelIcon())
		.addField("Old count", Integer.toString(source.getPrevValue()), true)
		.addField("New count", Integer.toString(channel.getSubCount()), true)
		.setColor(0xff0000);
		
		sendAlertMessage(alert, source);
		alert.clear();
	}
	
	/**
	 * Builds alert messages for Twitter Follower alerts.
	 * @param source - Source object to alert for
	 * @param user - User object that contains information about the alert
	 */
	private static void tweetFollowMessage(StatSource source, TwitterUser user) {
		// Don't send message if source is brand new
		if (source.getPrevValue() == -1) return;
		
		// Build embed
		EmbedBuilder alert = new EmbedBuilder()
		.setTitle("Twitter Follower alert for: " + user.getUserName(),
				"https://www.twitter.com/" + user.getUserHandle())
		.setThumbnail(user.getUserIcon())
		.addField("Old count", Integer.toString(source.getPrevValue()), true)
		.addField("New count", Integer.toString(user.getFollowCount()), true)
		.setColor(0x1da1f2);
		
		sendAlertMessage(alert, source);
		alert.clear();
	}
}
