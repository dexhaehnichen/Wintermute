
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.server.Server;

public class Wintermute {
	static HashMap<Server, Boolean> enabled = new HashMap<Server, Boolean>();
	//static String token = ""; token is now a command line argument

	public static void main(String[] args) {

		if (args[0].equals("-help")) {
			System.out.println(
					"This discord bot responds to any message containing \"I'm,\" \"Im,\" or \"I am\" with "
					+ "\"Hi, <text after wake sequence>, I'm Wintermute.\" This bot requires a valid bot token, passed as an argument. "
					+ "\nExample usage:"
					+ "\njava -jar Wintermute.jar <token>");
			System.exit(0);
		}

		DiscordApi api = null;
		try {
			api = new DiscordApiBuilder().setToken(args[0]).login().join();
		}catch(IllegalStateException e) {
			System.out.println("Invalid bot token. Use the -help flag for more help.");
			System.exit(0);
		}catch(CompletionException e) {
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
			System.out.println("Heard message: " + event.getMessageContent());
			if (event.getMessageContent().length() >= 5) {
				if (containsIm(event.getMessageContent()) == 0 && getEnabled(event.getServer())) {
					updateEnabled(event.getServer(), true);
					event.getChannel().sendMessage(reply(event.getMessageContent(), "i'm"));

				} else if (containsIm(event.getMessageContent()) == 1 && getEnabled(event.getServer())) {
					updateEnabled(event.getServer(), true);
					event.getChannel().sendMessage(reply(event.getMessageContent(), "im"));

				} else if (containsIm(event.getMessageContent()) == 2 && getEnabled(event.getServer())) {
					updateEnabled(event.getServer(), true);
					event.getChannel().sendMessage(reply(event.getMessageContent(), "i am"));
				}
			}
		});

		// Print the client id of the bot
		System.out.println("Logged in as " + api.getClientId());
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

	static String reply(String message, String substring) {
		System.out.println("sending message...");
		String reply = "";
		String s = message;
		for (int i = 0 + substring.length(); i < s.length(); i++) {
			if (s.charAt(i) == '.' || s.charAt(i) == '!' || s.charAt(i) == '?' || s.charAt(i) == ',') {
				if (s.charAt(i - 1) != ' ') {
					return "Hi," + reply + ". I'm Wintermute.";
				}
			}
			reply += s.charAt(i);
		}
		return "Hi," + reply + ". I'm Wintermute.";
	}

	static int containsIm(String s) {
		System.out.println("Testing containsIm...");
		if (s.toLowerCase().substring(0, 4).equals("i'm ")) {
			return 0;
			// case A: i'm
		} else if (s.toLowerCase().substring(0, 3).equals("im ")) {
			return 1;
			// case B: im
		} else if (s.toLowerCase().substring(0, 5).equals("i am ")) {
			return 2;
			// case C: i am
		} else {
			return -1;
		}
	}

}
