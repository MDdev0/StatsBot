package maertin.commands;

import java.util.Arrays;
import java.util.List;

import maertin.bot.StatPinger;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bug extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent msg) {
		List<String> args = Arrays.asList(msg.getMessage().getContentRaw().split("\\s+"));
		
		if (args.get(0).equalsIgnoreCase(StatPinger.PREFIX + "bug")) {
			msg.getChannel().sendMessage("Submit bug reports (and feature requests) on GitHub: https://github.com/MDdev0/StatsBot/issues/new/choose");
		}
	}
}
