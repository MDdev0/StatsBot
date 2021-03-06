package maertin.bot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

import javax.security.auth.login.LoginException;

import maertin.commands.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class StatPinger {
	// JDA info
	public static JDA jda;
	public static final String PREFIX = "$";
	public static long REFRESH_RATE = 20000L;
	public static int QUERY_TIMEOUT = 5000;
	
	/**
	 * Sources are stored as StatSource objects.<p>
	 * Each StatSource object contains another list of the Guilds listening to it.<p>
	 */
	public static ArrayList<StatSource> sources = new ArrayList<StatSource>();
	
	@SuppressWarnings("unused")
	private static AlertManager alerts;
	
	// Arguments: <JDA token> [Refresh rate] [Query timeout]
	public static void main(String[] args) throws LoginException, IOException, InterruptedException {
		// Interpret Arguments
		if (args.length == 0) {
			System.out.println("Unable to initialize bot without a token!");
			return;
		}
		switch (args.length) {
		case 3:
			QUERY_TIMEOUT = Integer.parseInt(args[2]);
			System.out.println("[LAUNCHING] Query Timeout set to: " + QUERY_TIMEOUT + "ms");
		//$FALL-THROUGH$
		case 2:
			REFRESH_RATE = Long.parseLong(args[1]);
			System.out.println("[LAUNCHING] Refresh Rate set to: " + REFRESH_RATE + "ms");
		}
		jda = JDABuilder.createLight(args[0]).build();
		jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.listening("error logs and loading saved sources!"));
		
		jda.awaitReady();
		
		loadAll();
		
		jda.addEventListener(new Bug());
		jda.addEventListener(new Help());
		jda.addEventListener(new Watch());
		jda.addEventListener(new Unwatch());
		
		alerts = new AlertManager();
	}
	
	/*
	 * FIXME: ADD A CLEAN SHUTDOWN FOR GOD'S SAKE!
	 */
	
	/**
	 * Loads all the saved sources into the new instance of the pinger.
	 */
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
						System.out.println("Not loading empty source: " + save.getName());
				} catch (IOException | NoSuchElementException e) {
					String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss;SSS").format(new Date());
					System.out.println("[" + timestamp + "] Unable to load source:");
					System.out.println(save.getAbsolutePath());
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Unable to create directory! No saves loaded.");
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
