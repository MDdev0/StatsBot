package maertin.commands;

import java.util.Arrays;
import java.util.List;

import maertin.bot.StatPinger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Help extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent msg) {
		List<String> args = Arrays.asList(msg.getMessage().getContentRaw().split("\\s+", 2));
		EmbedBuilder embed = new EmbedBuilder().setAuthor("Help for:").setColor(0x40dede)
				.setFooter(msg.getAuthor().getAsTag(), msg.getAuthor().getAvatarUrl());
		
		if (args.get(0).equalsIgnoreCase(StatPinger.PREFIX + "help")) {
			switch (args.size() > 1 ? args.get(1).trim().toLowerCase() : "") {
			case ("watch"):
				embed.setTitle("`watch`")
				.setDescription("Adds notifications for a source to your server.")
				.addField("Syntax:", "`" + StatPinger.PREFIX + "watch` `<link to source>` `<source type>`", false)
				.addField("Source links:",
						"**YouTube Channel:** `youtube.com/channel/####` OR `youtube.com/user/####`,\n"
						+ "Other Sources Coming Soon", false)
				.addField("Source types:",
						"`subscribers`, More Sources Coming Soon", false);
				break;
			case ("unwatch"):
				embed.setTitle("`unwatch`")
				.setDescription("Stops notifying your server about a particular source.")
				.addField("Syntax:", "`" + StatPinger.PREFIX + "unwatch` `<link to source>` `<source type>`", false)
				.addField("Source links:",
						"**YouTube Channel:** `youtube.com/channel/####` OR `youtube.com/user/####`", false)
				.addField("Source types:",
						"`subscribers`", false);
				break;
			case ("bug"):
				embed.setTitle("`bug`")
				.setDescription("Provides a link for bug reports and feature requests.")
				.addField("Syntax:", "`" + StatPinger.PREFIX + "bug`", false);
				break;
			case ("help"):
				embed.setTitle("`help`")
				.setDescription("Shows these help dialogs.")
				.addField("Syntax:", "`" + StatPinger.PREFIX + "help` `<command>`", false);
				break;
			default:
				embed.clear().setTitle("All commands:")
				.setDescription("Run `" + StatPinger.PREFIX + "help` `<command>` to see more details on each command.")
				.addField("`watch`", "Adds notifications for a source to your server.", false)
				.addField("`unwatch`", "Stops notifying your server about a particular source.", false)
				.addField("`bug`", "Provides a link for bug reports and feature requests.", false)
				.addField("`help`", "Shows these help dialogs.", false)
				.setColor(0x40dede).setFooter(msg.getAuthor().getAsTag(), msg.getAuthor().getAvatarUrl());
			}
			msg.getChannel().sendMessage(embed.build()).queue();
		}
	}
}
