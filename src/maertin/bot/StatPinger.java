package maertin.bot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		
		loadAll();
		
		jda.addEventListener(new Watch());
		
		alerts = new AlertManager();
	}
	
	/*
	 * TODO: Command to remove a source from your server
	 * TODO: Make the data collection from Mixerno a little more efficient
	 * FIXME: Save information between restarts
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
					FileInputStream loadFile = new FileInputStream(save);
					ObjectInputStream loadObj = new ObjectInputStream(loadFile);
					StatSource loadedSource = (StatSource) loadObj.readObject();
					sources.add(loadedSource);
					loadFile.close();
					loadObj.close();
				} catch (IOException | ClassNotFoundException e) {
					System.out.println("Unable to load source:");
					System.out.println(save.getAbsolutePath());
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("NOT A DIRECTORY! No saves loaded.");
		}
	}
	
	/**
	 * Saves all the sources from the pinger to the disk.
	 */
	public static void saveAll() {
		for (StatSource source : sources) {
			try {
				FileOutputStream saveFile = new FileOutputStream( "saves/" + (
								source.getType() == 0 ? "YTSub" : "UNKNOWN" // source.getType() == 0 ? "YTSub" : source.getType() == 1 ? "ETC" : "ETC"
							) + "_" + source.getID() + ".ser", false);
				
				ObjectOutputStream saveObj = new ObjectOutputStream(saveFile);
				saveObj.writeObject(source);
				saveObj.close();
				saveFile.close();
			} catch (IOException e) {
				System.out.println("Unable to save source:");
				System.out.println(source.getID() + " of type " + source.getType());
				e.printStackTrace();
			}
		}
	}
}
