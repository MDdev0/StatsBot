package maertin.bot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.security.auth.login.LoginException;

import maertin.commands.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class StatPinger {
	// JDA info
	public static JDA jda;
	// Config values
	private static String DISCORD_API_KEY;
	public static String YOUTUBE_API_KEY;
	public static String PREFIX;
	public static long REFRESH_RATE;
	public static int QUERY_TIMEOUT;

	/**
	 * Sources are stored as StatSource objects.<p>
	 * Each StatSource object contains another list of the Guilds listening to it.<p>
	 */
	public static ArrayList<StatSource> sources = new ArrayList<>();

	@SuppressWarnings("unused")
	private static AlertManager alerts;

	public static void main(String[] args) throws LoginException, IOException, InterruptedException {
		// Interpret Config
		// FIXME: Find a way to move default config back to root, then remove that from gitignore
		File config = new File("StatsBot.config");
		if (!config.exists()) {
			System.out.println("[Startup] Could not find configuration file, creating a new one");
			Files.copy(Objects.requireNonNull(StatPinger.class.getResourceAsStream("StatsBot.config")), config.toPath());
		}
		try {
			setupConfig(config);
		} catch (Exception ex) {
			System.out.println("""
					Exception while initializing configuration!
					Check your configuration file or delete it to generate a fresh one.
					You may need to delete and regenerate the config after an update, or add missing parameters.""");
			ex.printStackTrace();
			return;
		}
		jda = JDABuilder.createLight(DISCORD_API_KEY).build();
		jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.listening("error logs and loading saved sources!"));
		
		jda.awaitReady();
		
		loadAll();
		
		jda.addEventListener(new Bug());
		jda.addEventListener(new Help());
		jda.addEventListener(new Watch());
		jda.addEventListener(new Unwatch());

		alerts = new AlertManager();
	}

	// TODO: Add a logger at some point b/c using println is dirty

	/*
	 * FIXME: ADD A CLEAN SHUTDOWN FOR GOD'S SAKE!
	 */
	private static void setupConfig(File configFile) throws IOException {
		ArrayList<String> cfg = new ArrayList<>(Arrays.asList(Files.readString(configFile.toPath()).split("[\n=]")));
		DISCORD_API_KEY = cfg.get(cfg.indexOf("discord-api-key") + 1).trim();
		System.out.println("[Startup] Set Discord API Key: " + DISCORD_API_KEY);
		YOUTUBE_API_KEY = cfg.get(cfg.indexOf("youtube-api-key") + 1).trim();
		System.out.println("[Startup] Set YouTube API Key: " + YOUTUBE_API_KEY);
		PREFIX = cfg.get(cfg.indexOf("prefix") + 1).trim();
		System.out.println("[Startup] Set global prefix: " + PREFIX);
		REFRESH_RATE = Long.decode(cfg.get(cfg.indexOf("refresh-rate") + 1).trim());
		System.out.println("[Startup] Set refresh rate: " + REFRESH_RATE + " ms");
		QUERY_TIMEOUT = Integer.parseInt(cfg.get(cfg.indexOf("web-query-timeout") + 1).trim());
		System.out.println("c Set web query timeout: " + QUERY_TIMEOUT + " ms");
	}

	/**
	 * Loads all the saved sources into the new instance of the pinger.
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void loadAll() {
		// Make the saves folder if none exists in this location:
		File saveDir = new File("saves");
		if (!saveDir.exists()) saveDir.mkdir();

		File[] saves = saveDir.listFiles();
		if (saves != null) {
			for (File save : saves) {
				try {
					StatSource source = new StatSource();
					source.deserialize(save);
					// Only add the source to the array if there are any servers listening to it, otherwise discard
					if (!source.isEmpty())
						sources.add(source);
					else
						System.out.println("[Startup] Not loading empty source: " + save.getName());
				} catch (IOException | NoSuchElementException e) {
					String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
					System.out.println("[" + timestamp + "] Unable to load source:");
					System.out.println(save.getAbsolutePath());
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("[Startup] Unable to create directory! No saves loaded.");
		}
	}
	
	/**
	 * Saves all the sources from the pinger to the disk.
	 */
	public static void saveAll() {
		for (StatSource source : sources) {
			try {
				File saveFile = new File( "saves/" + source.getType() + "_" + source.getID() + ".dat");
				source.serialize(saveFile);
			} catch (IOException e) {
				String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
				System.out.println("[" + timestamp + "] Unable to save source:");
				System.out.println(source.getID() + " of type " + source.getType());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes an item from the saves if it needs to be deleted.
	 */
	public static void clearFile(StatSource source) {
		File saveFile = new File( "saves/" + source.getType() + "_" + source.getID() + ".dat");
		String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
		try {
			if (saveFile.delete()) {
				System.out.println("[" + timestamp + "] Source deleted:");
				System.out.println(source.getID() + " of type " + source.getType());
			} else {
				System.out.println("[" + timestamp + "] Could not delete source:");
				System.out.println(source.getID() + " of type " + source.getType());
			}
		} catch (Exception e) {
			System.out.println("[" + timestamp + "] Error deleting source:");
			System.out.println(source.getID() + " of type " + source.getType());
			e.printStackTrace();
		}
	}
}
