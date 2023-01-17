package smcc.cis;

import com.google.gson.Gson;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/* A list of everyone to ever work on the bot and when they first started working on it:
 * Alexander Hill (March 10, 2021)
 * Anne Applin (December 29, 2020)
 * Coley Hatt (Janurary 16, 2023)
 */

/**
 * A bot to assign roles to students on the SMCC CSCI server
 * 
 * @author aapplin
 */
public class RoleBot {

	/**
	 * roles is a map that connects server role names to the ids of those roles on
	 * the discord server
	 */
	private static HashMap<String, String> roles = new HashMap<String, String>(); // Dat O(1) ~Alex

	/**
	 * addRole() adds this user to the roles they requested
	 * 
	 * @author Anne Applin
	 * 
	 * @param argList the message string that is one or more roles in the map
	 * @param event   the event that triggered the bot allows us access to the
	 *                user's username on the server
	 * @param api     the discord api that allows us to access the server roles and
	 *                add this particular user
	 */
	private static void addRole(String[] argList, MessageCreateEvent event, DiscordApi api) {
		// Initialize all our variables
		int added = 0;
		TextChannel chan = event.getChannel();
		User user = event.getMessageAuthor().asUser().get();
		
		// Iterate over every argument and check if it's a valid role
		for (String arg.toUpperCase() : argList) {
			if (roles.containsKey(arg.toUpperCase())) {
				// If it is, add the role to the person who requested it.
				Role role = api.getRoleById(roles.get(arg.toUpperCase())).get();
				role.addUser(user);
				added++;
			}
		}
		
		// Finally, send a message in chat reflecting the changes made to the user.
		if (argList.length == 0) {
			chan.sendMessage("You must specify a role to add!");
		} else if (argList.length == 1) {
			chan.sendMessage(String.format("%s added!", argList[0]));
		} else {
			chan.sendMessage(String.format("%d roles added!", added));
		}
	}

	/**
	 * removeRole() removes this user from the roles they requested
	 * 
	 * @author Anne Applin
	 * 
	 * @param argList the message string that is one or more roles in the map
	 * @param event   the event that triggered the bot allows us access to the
	 *                user's username on the server
	 * @param api     the discord api that allows us to access the server roles and
	 *                remove this particular user
	 */
	private static void removeRole(String[] argList, MessageCreateEvent event, DiscordApi api) {
		// Initialize all our variables
		TextChannel chan = event.getChannel();
		int removed = 0;
		User user = event.getMessageAuthor().asUser().get();

		// Iterate over every argument to check if it's a valid role
		for (String arg.toUpperCase() : argList) {
			if (roles.containsKey(arg.toUpperCase())) {
				// If it is, remove the role from the person who made the request.
				Role role = api.getRoleById(roles.get(arg.toUpperCase())).get();
				role.removeUser(user);
				removed++;
			}
		}
		
		// Finally, send a message in chat reflecting the changes made to the user.
		if (argList.length == 0) {
			chan.sendMessage("You must specify a role to remove!");
		} else if (argList.length == 1) {
			chan.sendMessage(String.format("%s removed!", argList[0]));
		} else {
			chan.sendMessage(String.format("%d roles removed!", removed));
		}
	}

	/**
	 * Rolls a 6-sided die and prints the result in chat
	 * 
	 * @author Alexander Hill
	 * 
	 * @param event The event associated with this command
	 * @param api   Access to the Discord API via JavaCord
	 */
	private static void rollCommand(MessageCreateEvent event, DiscordApi api) {
		// This can be used to roll multiple dice, n-sided dice, etc.
		// Is anybody up to the task? ~Alex
		
		// Initialize our variables
		MessageAuthor author = event.getMessage().getAuthor();
		TextChannel chan = event.getChannel();
		Random rand = new Random();
		
		// Roll a random number between 1 and 6
		int faceValue = rand.nextInt(6) + 1;
		
		// Send the result in chat
		chan.sendMessage(String.format("<@%d> rolled a %d", author.getId(), faceValue));
	}
	
	/**
	 * Main method where the action is.
	 * 
	 * @author Anne Applin
	 * 
	 * @param args Command line arguments - unused
	 */
	public static void main(String[] args) {
		String token = null;

		try {
			Gson gson = new Gson();
			Reader reader = Files.newBufferedReader(Paths.get("config.json"));
			Map<?, ?> map = gson.fromJson(reader, Map.class);
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				token = entry.getValue().toString();
			}
			reader.close();
			reader = Files.newBufferedReader(Paths.get("roles.json"));
			Map<?, ?> rolesIn = gson.fromJson(reader, Map.class);
			rolesIn.forEach((key, value) -> {
				roles.put(key.toString(), value.toString());
			});
			reader.close();
			DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

			api.addMessageCreateListener(event -> {
				if (event.getMessageContent().startsWith("?")) {
					String message = event.getMessageContent().substring(1);
					System.out.println(message);
					String[] argList = message.split(" ");
					System.out.println(argList.length);
					switch (argList[0].toLowerCase()) {
					case "add":
						addRole(argList, event, api);
						break;
					case "remove":
						removeRole(argList, event, api);
						break;
					case "roll": // RollBot, get it? (I hate myself for this already) ~Alex
						rollCommand(event, api);
						break;
					case "rps": // Rock, Paper, Scissors!
						RockPaperScissors.command(argList, event, api);
						break;
					default:
						break;
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
