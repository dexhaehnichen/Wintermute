
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.server.Server;

public class Wintermute {
	static HashMap<Server, Boolean> enabled = new HashMap<Server, Boolean>();

	public static void main(String[] args) {
		DiscordApi api = null;
		
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
			String firstSentence = event.getMessageContent().split("[,?!.]")[0].strip();

			System.out.println("Heard message: " + event.getMessageContent());
			if (firstSentence.length() >= 5) {
				if (containsIm(firstSentence, "i'm") && getEnabled(event.getServer())) {
					event.getChannel().sendMessage("Hi,"
							+ firstSentence.substring("i'm".length(), firstSentence.length()) + ". I'm Wintermute.");

				} else if (containsIm(firstSentence, "im") && getEnabled(event.getServer())) {
					event.getChannel().sendMessage("Hi,"
							+ firstSentence.substring("im".length(), firstSentence.length()) + ". I'm Wintermute.");

				} else if (containsIm(firstSentence, "i am") && getEnabled(event.getServer())) {
					event.getChannel().sendMessage("Hi,"
							+ firstSentence.substring("i am".length(), firstSentence.length()) + ". I'm Wintermute.");
				}
			}
		});

		// Print the client id of the bot to know when and/or whether the bot managed to connect.  
		System.out.println("Logged in as " + api.getClientId());
	}

	static boolean containsIm(String s, String test) {
		System.out.println(s.charAt(test.length()) == ' ');

		if (s.toLowerCase().substring(0, test.length()).equals(test)
		 && s.charAt(test.length()) == ' ') {
			return true;
		} else {
			return false;
		}
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
