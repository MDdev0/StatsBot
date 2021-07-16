package maertin.dataFetchers;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ScrapeTest {
	
	final static String URL = "https://beta.mixerno.space/youtube-subscriber-counter/UCDpXkkO-zrCJBByrdERqKMw";
	
	static TimerTask getData = new TimerTask() {
		@Override
		public void run() {
			try {
				Document doc = Jsoup.connect(URL).timeout(5000).get();
				
				String digits = doc.getElementById("mainodo").text().replaceAll(",", "");
				
				long count = Integer.parseInt(digits);
				System.out.println("Subscribers: " + count);
				
			} catch (SocketTimeoutException e) {
				System.out.println("Timed out.");
			} catch (IOException e) {
				System.out.println("UNABLE TO GET WEBPAGE!");
				e.printStackTrace();
			}
		}
	};
	
	static Timer masterTimer = new Timer();
	
	static Scanner userIn = new Scanner(System.in);
	
	public static void main(String[] args) {
		masterTimer.scheduleAtFixedRate(getData, 0, 10000);
		
		//waiting for enter to close
		userIn.nextLine();
		getData.cancel();
		masterTimer.cancel();
		userIn.close();
	}
	
}
