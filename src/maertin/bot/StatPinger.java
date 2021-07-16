package maertin.bot;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import maertin.commands.Watch;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class StatPinger {
	// JDA info
	private static final String TOKEN = 
			"ODU0MDczMjIwODY0MDE2NDQ0.YMenqw.OZLQ5yn_evfWQlFou20M9zpP52w";
	public static JDA jda;
	public static final String PREFIX = "$";
	public static final long REFRESH_RATE = 10000L;
	
	/**
	 * Sources are stored as StatSource objects.<p>
	 * Each StatSource object contains another list of the Guilds listening to it.<p>
	 */
	public static ArrayList<StatSource> sources = new ArrayList<StatSource>();
	
	@SuppressWarnings("unused")
	private static AlertManager alerts;
	
	public static void main(String[] args) throws LoginException, IOException {
		jda = JDABuilder.createLight(TOKEN).build();
		jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.listening("for errors and loading saved sources!"));
		
		jda.addEventListener(new Watch());
		
		alerts = new AlertManager();
	}
	
	/*
	 * TODO: Command to remove a source from your server
	 * TODO: Make the data collection from Mixerno a little more efficient
	 * FIXME: Save information between restarts
	 * FIXME: ADD A CLEAN SHUTDOWN FOR GOD'S SAKE!
	 */
}
