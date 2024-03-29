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

public class Unwatch extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent msg) {
		// Return messages need to be reset every time
		final EmbedBuilder SUCCESS = new EmbedBuilder().setTitle("✅ Success!").setColor(0x2dd52d)
				.setDescription("Your guild will no longer be notified about the following source. Use `" + StatPinger.PREFIX + "watch` to add it again later.");
		final EmbedBuilder ERROR_UNKNOWN = new EmbedBuilder().setTitle("⛔ Error").setColor(0xe0143e).setDescription(
				"An unknown error occurred while trying to remove this source. Maybe try another one or try again later?")
				.setFooter("This message will be deleted in 60 seconds.");
		final EmbedBuilder WARNING_NOT_FOUND = new EmbedBuilder().setTitle("⚠ Source Not Found").setColor(0xefef32).setDescription(
				"Unable to remove the source because this server is not listening to it!").setFooter("This message will be deleted in 60 seconds.");
		final EmbedBuilder WARNING_SYNTAX = new EmbedBuilder().setTitle("⚠ Improper Syntax").setColor(0xefef32).setFooter("This message will be deleted in 60 seconds.")
				.addField("YouTube Subscriber Count:", "`" + StatPinger.PREFIX + "unwatch` `youtube.com/channel/#######` `subscribers`", false)
				.addField("Twitter Follower Count:", "`" + StatPinger.PREFIX + "unwatch` `twitter.com/#######` `followers`", false);
	
		List<String> args = Arrays.asList(msg.getMessage().getContentRaw().split("\\s+"));
		
		if (args.get(0).equalsIgnoreCase(StatPinger.PREFIX + "unwatch")) {
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
						
						// Find source
						int index = StatPinger.sources.indexOf(new StatSource(channel.getChannelID(), SourceType.YTSub));
						// If source isn't found
						if (index == -1) {
							msg.getChannel().sendMessage(WARNING_NOT_FOUND.build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
							return; // ENDS HERE IF NOT FOUND
						}
						
						// Remove guild from source
						StatSource source = StatPinger.sources.get(index);
						if (!source.remove(msg.getGuild())) {
							msg.getChannel().sendMessage(WARNING_NOT_FOUND.build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
							return; // ENDS HERE IF NOT FOUND
						}
						
						if (source.size() == 0) // Delete the source altogether if none are left
							StatPinger.sources.remove(index);
						else // Replace the source in the list with the updated one
							StatPinger.sources.set(index, source);
						
						// Clear the file
						StatPinger.clearFile(source);
						
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
						
						// Find source
						int index = StatPinger.sources.indexOf(new StatSource(user.getUserHandle(), SourceType.TweetFollow));
						// If source isn't found
						if (index == -1) {
							msg.getChannel().sendMessage(WARNING_NOT_FOUND.build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
							return; // ENDS HERE IF NOT FOUND
						}
						
						// Remove guild from source
						StatSource source = StatPinger.sources.get(index);
						if (!source.remove(msg.getGuild())) {
							msg.getChannel().sendMessage(WARNING_NOT_FOUND.build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
							return; // ENDS HERE IF NOT FOUND
						}
						
						if (source.size() == 0) // Delete the source altogether if none are left
							StatPinger.sources.remove(index);
						else // Replace the source in the list with the updated one
							StatPinger.sources.set(index, source);
						
						// Clear the file
						StatPinger.clearFile(source);
						
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
				else
					msg.getChannel().sendMessage(WARNING_SYNTAX.setDescription("I couldn't understand that request! Here's the proper syntax:").build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
			}
		}
	}
}
