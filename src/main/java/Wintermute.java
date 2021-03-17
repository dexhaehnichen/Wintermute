
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.server.Server;

public class Wintermute {
	static HashMap<Server, Boolean> enabled = new HashMap<Server, Boolean>();
	static DiscordApi api = null;

	public static void main(String[] args) {

		String[] variationsOfIm = { "i'm", "im", "i am", "l'm", "lm", "l am" };

		// Since Wintermute is meant to be run from the command line, I have this little
		// help message for people
		// who don't know what they're doing.

		if (args.length == 0 || args[0].equals("-help")) {
			System.out.println("This discord bot responds to any message containing \"I'm,\" \"Im,\" or \"I am\" with "
					+ "\"Hi, <text after wake sequence>, I'm Wintermute.\" This bot requires a valid bot token, passed as an argument. "
					+ "\nExample usage:" + "\njava -jar Wintermute.jar <token>");
			System.exit(0);
		}

		try {
			api = new DiscordApiBuilder().setToken(args[0]).login().join();
		} catch (IllegalStateException e) {
			System.out.println("Invalid bot token. Use the -help flag for more help.");
			System.exit(0);
		} catch (CompletionException e) {
			System.out.println("Invalid bot token. Use the -help flag for more help.");
			System.exit(0);
		}

		// listens for system commands
		api.addMessageCreateListener(event -> {
			// listen for enable/disable relevant commands
			if (event.getMessageContent().equalsIgnoreCase("Wintermute: enable")
					&& event.getMessageAuthor().isServerAdmin()) {
				updateEnabled(event.getServer(), true);
				event.getChannel().sendMessage("Enabled = " + enabled.get(event.getServer().get()));
			} else if (event.getMessageContent().equalsIgnoreCase("Wintermute: disable")
					&& event.getMessageAuthor().isServerAdmin()) {
				updateEnabled(event.getServer(), false);
				event.getChannel().sendMessage("Enabled = " + enabled.get(event.getServer().get()));
			}

			// listen for status queries
			if (event.getMessageContent().equalsIgnoreCase("Wintermute: status")
					&& event.getMessageAuthor().isServerAdmin()) {
				event.getChannel().sendMessage("online; Enabled = " + enabled.get(event.getServer().get()));
			}

			// only the owner can see the full hashmap of servers and their states
			if (event.getMessageContent().equalsIgnoreCase("Wintermute: fullStatus")
					&& event.getMessageAuthor().isServerAdmin() && event.getMessageAuthor().isBotOwner()) {
				System.out.println("full status: " + enabled.toString());
			}
		});

		// listen for messages with "I'm", "Im", or "I am" in them
		api.addMessageCreateListener(event -> {
			String firstSentence = event.getMessageContent().split("[,?!.]")[0].strip().toLowerCase();

			System.out.println("Heard message: " + event.getMessageContent());
			if (firstSentence.length() >= 5 && getEnabled(event.getServer())) {
				// I have Wintermute set to trigger on lowercase 'L's because they look
				// identical to uppercase 'I's in
				// discord.
				if (containsIm(firstSentence, variationsOfIm)) {
					event.getChannel().sendMessage(makeReply(firstSentence, "i'm", api, event.getServer().get()));
				}
			}
		});

		// Print the client id of the bot to know when and/or whether the bot managed to
		// connect.
		System.out.println("Logged in as " + api.getClientId());

	}

	static boolean containsIm(String stringToTest, String[] testStrings) {
		boolean containsIm = false;
		
		// For each test string, check if the string to test starts with the test string.
		for (int i = 0; i < testStrings.length; i++) {
			if(stringToTest.startsWith(testStrings[i] + " ")) {
				containsIm = true;
				
				// If the string to test starts with the test string, for each test string, see if the 
				// part of the string to test that comes after the first test string starts with-
				// wait a second...
				// I think I have a better idea on how to do this.
				
				for (int j = 0; j < testStrings.length; j++) {
					if(stringToTest.substring(testStrings[i].length()+1).startsWith(testStrings[j] + " ")) {
						containsIm = false;
					}
				}
			}
		}
		
		return containsIm;
	}

	static String makeReply(String sentence, String imType, DiscordApi api, Server server) {
		String nickname;

		if (api.getYourself().getNickname(server).isEmpty()) {
			nickname = "Wintermute";
		} else {
			nickname = api.getYourself().getNickname(server).get();
		}

		return "Hi," + sentence.substring(imType.length(), sentence.length()) + ". I'm " + nickname;
	}

	static boolean getEnabled(Optional<Server> server) {
		enabled.putIfAbsent(server.get(), true);
		return enabled.get(server.get());
	}

	static void updateEnabled(Optional<Server> server, boolean b) {
		System.out.println("Updating enabled to " + b + " for server " + server.get());
		enabled.put(server.get(), b);
		System.out.println(enabled.toString());
	}
}
