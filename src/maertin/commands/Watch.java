package maertin.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import maertin.bot.StatPinger;
import maertin.bot.StatSource;
import maertin.bot.StatSource.SourceType;
import maertin.dataFetchers.TwitterUser;
import maertin.dataFetchers.YTChannel;
import maertin.dataFetchers.YTData;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Watch extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent msg) {
		// These need to be reset every message
		final EmbedBuilder SUCCESS = new EmbedBuilder().setTitle("✅ Success!").setColor(0x2dd52d)
				.setDescription("Your guild will now be notified about the following source. Use `" + StatPinger.PREFIX + "unwatch` to remove it later.");
		final EmbedBuilder ERROR_UNKNOWN = new EmbedBuilder().setTitle("⛔ Error").setColor(0xe0143e).setDescription(
				"An unknown error occured while trying to watch this source. Maybe try another one or try again later?")
				.setFooter("This message will be deleted in 60 seconds.");
		final EmbedBuilder WARNING_SYNTAX = new EmbedBuilder().setTitle("⚠ Improper Syntax").setColor(0xefef32).setFooter("This message will be deleted in 60 seconds.")
				.addField("YouTube Subscriber Count:", "`" + StatPinger.PREFIX + "watch` `youtube.com/channel/#######` `subscribers`", false)
				.addField("Twitter Follower Count:", "`" + StatPinger.PREFIX + "watch` `twitter.com/#######` `followers`", false);
		
		List<String> args = Arrays.asList(msg.getMessage().getContentRaw().split("\\s+"));
		
		if (args.get(0).equalsIgnoreCase(StatPinger.PREFIX + "watch")) {
			// Arguments check
			if (args.size() < 3) {
				msg.getChannel().sendMessage(WARNING_SYNTAX.setDescription("Not enough arguments! Here's the proper syntax:").build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
			} else {
				
				//=======================
				// Check if type is YouTube Subscribers
				if ( (args.get(1).contains("youtube.com/channel/") || args.get(1).contains("youtube.com/user/"))
						&& args.get(2).equalsIgnoreCase("subscribers")) {
					try {
						YTChannel channel = new YTChannel(YTData.getChannelID(args.get(1)));
						StatSource newSource = new StatSource(channel.getChannelID(), SourceType.YTSub);
						
						// Add the guild to the list of guilds if the source already exists
						if (StatPinger.sources.contains(newSource)) {
							int index = StatPinger.sources.indexOf(newSource);
							// Copy current data from list into newSource
							newSource = StatPinger.sources.get(index);
							// Add the guild to the list
							newSource.add(msg.getGuild());
							// Put it back
							StatPinger.sources.set(index, newSource);
						}
						// Otherwise, add a new source and add the guild
						else {
							newSource.add(msg.getGuild());
							StatPinger.sources.add(newSource);
						}
						// Success Message
						msg.getChannel().sendMessage(SUCCESS.addField(channel.getChannelName(), "**Tracking:** Subscriber Count", false)
								.setThumbnail(channel.getChannelIcon()).build()).queue();
					} catch (Exception e) {
						// XXX Maybe be a little more descriptive? Just an option.
						// Error Message
						msg.getChannel().sendMessage(ERROR_UNKNOWN.build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
						e.printStackTrace();
					}
				}
				//=======================
				// Check if type is Twitter Followers
				else if ( (args.get(1).contains("twitter.com/")
						&& args.get(2).equalsIgnoreCase("followers"))) {
					try {
						TwitterUser user = new TwitterUser(args.get(1).substring(args.get(1).indexOf("twitter.com/") + 12));
						StatSource newSource = new StatSource(user.getUserHandle(), SourceType.TweetFollow);
						
						// Add the guild to the list of guilds if the source already exists
						if (StatPinger.sources.contains(newSource)) {
							int index = StatPinger.sources.indexOf(newSource);
							// Copy current data from list into newSource
							newSource = StatPinger.sources.get(index);
							// Add the guild to the list
							newSource.add(msg.getGuild());
							// Put it back
							StatPinger.sources.set(index, newSource);
						}
						// Otherwise, add a new source and add the guild
						else {
							newSource.add(msg.getGuild());
							StatPinger.sources.add(newSource);
						}
						// Success Message
						msg.getChannel().sendMessage(SUCCESS.addField(user.getUserName(), "**Tracking:** Follower Count", false)
								.setThumbnail(user.getUserIcon()).build()).queue();
						
					} catch (Exception e) {
						// XXX Maybe be a little more descriptive? Just an option.
						// Error Message
						msg.getChannel().sendMessage(ERROR_UNKNOWN.build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
						e.printStackTrace();
					}
				} 
				//=======================
				// Syntax Error
				else
					msg.getChannel().sendMessage(WARNING_SYNTAX.setDescription("I couldn't understand that request! Here's the proper syntax:").build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
			}
		}
	}
}
