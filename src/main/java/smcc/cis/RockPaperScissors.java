package smcc.cis;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;

public class RockPaperScissors {
    // Someone should introduce "LIZARD" and "SPOCK" to the mix. ~Alex

    /**
     * A list of which choices can defeat which other choices
     */
    private static final Map<Choice, Choice[]> CAN_DEFEAT = Map.of(Choice.ROCK, new Choice[] { Choice.SCISSORS },
            Choice.PAPER, new Choice[] { Choice.ROCK }, Choice.SCISSORS, new Choice[] { Choice.PAPER });

    /**
     * All the possible choices to choose from
     */
    public enum Choice {
        ROCK, PAPER, SCISSORS
    }

    /**
     * Who won the round
     */
    public enum Result {
        COMPUTER, HUMAN, TIE, INDETERMINATE
    }

    /**
     * Converts a user-provided string into a choice
     * 
     * @author Alexander Hill
     * @param choice The choice of the user as represented as a string
     * @return A machine-readable version of the user's choice or null if the choice
     *         was invalid
     */
    public static Choice choiceFromString(String choice) {
        switch (choice.toLowerCase()) {
        case "rock":
            return Choice.ROCK;
        case "paper":
            return Choice.PAPER;
        case "scissors":
            return Choice.SCISSORS;
        default:
            return null;
        }
    }

    /**
     * Converts a choice into a human-readable string
     * 
     * @param choice The choice to convert
     * @return A textual representation of a choice or null on error
     */
    public static String choiceToString(Choice choice) {
        switch (choice) {
        case ROCK:
            return "Rock";
        case PAPER:
            return "Paper";
        case SCISSORS:
            return "Scissors";
        default:
            return null;
        }
    }

    /**
     * Plays a round of Rock, Paper, Scissors against the computer
     * 
     * @param args Arguments from the Discord command handler
     */
    public static void command(String[] args, MessageCreateEvent event, DiscordApi api) {
        Choice computer = randomChoice();
        Choice human = choiceFromString(args[0]);
        Result result = duel(human, computer);
        MessageAuthor author = event.getMessage().getAuthor();
        TextChannel chan = event.getChannel();
        chan.sendMessage(String.format("<@%d> %s vs. %s: %s", author.getId(), choiceToString(human),
                choiceToString(computer), resultToString(result)));
    }

    /**
     * Decides the winner between two choices in Rock, Paper, Scissors
     * 
     * @author Alexander Hill
     * @param human    The choice decided upon by the user
     * @param computer The choice decided upon by the computer
     * @return The winner of the round
     */
    public static Result duel(Choice human, Choice computer) {
        // The round is indeterminate if it was unclear what choice either side made
        if (human == null || computer == null)
            return Result.INDETERMINATE;

        // Check to see if both choices are the same
        if (human == computer)
            return Result.TIE;

        // Make sure we know what the user's choice can beat
        if (!CAN_DEFEAT.containsKey(human))
            return Result.INDETERMINATE;

        // Check to see if we can beat the computer's choice
        List<Choice> defeats = Arrays.asList(CAN_DEFEAT.get(human));
        if (defeats.contains(computer))
            return Result.HUMAN;
        else
            return Result.COMPUTER;
    }

    /**
     * Picks a random possible choice and returns it
     * 
     * @author Alexander Hill
     * @return A randomly picked choice
     */
    public static Choice randomChoice() {
        Choice[] choices = Choice.values();
        Random rand = new Random();
        return choices[rand.nextInt(choices.length)];
    }

    /**
     * Converts a result into a human-readable string
     * 
     * @param result Result of a round of Rock, Paper, Scissors
     * @return A human-readable version of the result
     */
    public static String resultToString(Result result) {
        switch (result) {
        case HUMAN:
            return "You have won!";
        case COMPUTER:
            return "You have lost!";
        case TIE:
            return "You tied!";
        case INDETERMINATE:
            return "The round is indeterminate! Make sure any new options have been implemented properly!";
        default: // This shouldn't happen but my IDE is yelling at me. ~Alex
            return null;
        }
    }
}