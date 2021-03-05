package smcc.cis;

import com.google.gson.Gson;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.TreeMap;

/**
 * A bot to assign roles to students on the SMCC CSCI server
 * @author aapplin
 */
public class RoleBot  {

    /**
     * roles is a map that connects server role names to the id's of those
     * roles on the discord server
     */
    private static TreeMap<String, String> roles = new TreeMap();

    /**
     * addRole() adds this user to the roles they requested
     * @param argList the message string that is one or more roles in the map
     * @param event the event that triggered the bot allows us access to the
     *              user's username on the server
     * @param api the discord api that allows us to access the server roles and
     *            add this particular user
     */
    public static void addRole(String[] argList, MessageCreateEvent event, DiscordApi api) {
        User user = event.getMessageAuthor().asUser().get();
        for (int i = 1; i < argList.length; i++) {
            if (roles.containsKey(argList[i])) {
                Role role = api.getRoleById(roles.get(argList[i])).get();
                role.addUser(user);
                event.getChannel().sendMessage("Role added!");
            }
        }
    }
    /**
     * removeRole() removes this user from the roles they requested
     * @param argList the message string that is one or more roles in the map
     * @param event the event that triggered the bot allows us access to the
     *              user's username on the server
     * @param api the discord api that allows us to access the server roles and
     *            remove this particular user
     */
    public static void removeRole(String[] argList, MessageCreateEvent event, DiscordApi api) {
        User user = event.getMessageAuthor().asUser().get();
        for (int i = 1; i < argList.length; i++) {
            if (roles.containsKey(argList[i])) {
                Role role = api.getRoleById(roles.get(argList[i])).get();
                role.removeUser(user);
                event.getChannel().sendMessage("Role removed!");
            }
        }
    }

    /**
     * Main method where the action is.
     * @param args Command line arguments - unused
     */
    public static void main(String[] args) {
        String token = null;

        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get("config.json"));
            Map<?, ?> map = gson.fromJson(reader,Map.class );
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
                        default:
                            break;
                    }
                }
            });
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
