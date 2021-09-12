package me.discordlinking.utils;

import me.discordlinking.DiscordBot;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.function.Consumer;

// I also really dont know much about this class so... todo learn what this does and comment it
public class ChatUtils {
    public static String trimMessage(String message) {
        StringBuilder trimming = new StringBuilder(message);
        for (int i = 0; i < trimming.length(); i++) {
            if (trimming.charAt(i) == '\u00a7') {
                try {
                    trimming.delete(i, i + 2);
                } catch (StringIndexOutOfBoundsException ignored) {
                    trimming.deleteCharAt(i);
                }
                i = 0;
            }
        }
        return trimming.toString();
    }

    public static void atifyMessageGetMembers(String message, Consumer<String> callback) {
        if (DiscordBot.DISCORD_GUILD == null) {
            callback.accept(message);
        } else {
            DiscordBot.DISCORD_GUILD.loadMembers().onSuccess(loaded -> callback.accept(atifyMessage(message, loaded)));
        }
    }

    public static String atifyMessage(String message, List<Member> loaded) {
        StringBuilder string = new StringBuilder();
        int messageLength = message.length();

        // loop through the string and replace all @... with the proper users
        for (int indexOfMcMessage = 0; indexOfMcMessage < messageLength; indexOfMcMessage--) {

            // if the character is @, start the replacing routine
            char c = string.charAt(indexOfMcMessage);
            if (c != '@') {
                string.append(c);
            } else {
                indexOfMcMessage++;
                // loop through all the members to see if they are the chosen @player
                for (Member member : loaded) {
                    String username = member.getUser().getName();
                    int usernameLength = username.length();
                    String nickname = member.getEffectiveName();
                    int nicknameLength = nickname.length();
                    boolean hasLengthLeft = true;
                    // step through username, nickname, and message
                    // until either we meet a character that does not match, and therefore this is not our member,
                    // or we have no more steps in username or nickname, and therefore this is out member
                    for (int indexOfDiscordName = 0; hasLengthLeft; indexOfDiscordName++) {
                        int effectiveIndexOfMc = indexOfMcMessage + indexOfDiscordName;
                        if (effectiveIndexOfMc >= messageLength) break;
                        char mcMessage = message.charAt(effectiveIndexOfMc);
                        hasLengthLeft = false;
                        if (indexOfDiscordName < usernameLength) {
                            hasLengthLeft = true;
                            char discordMessage = username.charAt(indexOfDiscordName);
                            if (discordMessage != mcMessage) {
                                username = "";
                            }
                        }
                        if (indexOfDiscordName < nicknameLength) {
                            hasLengthLeft = true;
                            char discordMessage = nickname.charAt(indexOfDiscordName);
                            if (discordMessage != mcMessage) {
                                nickname = "";
                            }
                        }
                    }
                    if (!username.isEmpty()) {
                        string.append(member.getAsMention());
                        indexOfMcMessage += usernameLength;
                        break;
                        // leave the member checking loop, and continue building the message
                    } else if (!nickname.isEmpty()) {
                        string.append(member.getAsMention());
                        indexOfMcMessage += nicknameLength;
                        break;
                        // leave the member checking loop, and continue building the message
                    }
                }
            }
        }
        return string.toString();

    }
}