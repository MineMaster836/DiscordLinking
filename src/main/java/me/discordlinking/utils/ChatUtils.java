package me.discordlinking.utils;

import me.discordlinking.DiscordBot;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Objects;

public class ChatUtils { // I also really dont know much about this class so... todo learn what this does and comment it
    public static String trimMessage(String message) {
        StringBuilder trimming = new StringBuilder(message);
        return trim(trimming);
    }

    private static String trim(StringBuilder string) {
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '\u00a7') {
                try {
                    string.delete(i, i + 2);
                } catch (StringIndexOutOfBoundsException ignored) {
                    string.deleteCharAt(i);
                }
                i = 0;
            }
        }
        return string.toString();
    }

    public static String atifyMessage(String message) {
        StringBuilder string = new StringBuilder(message);
        for (int i = string.length() - 1; i >= 0; i--) {
            if (string.charAt(i) == '@') {
                String atPlayer;
                List<User> users = DiscordBot.client.getUsers();
                getAt:
                for (int size = 1; size + i < string.length(); size++) {
                    atPlayer = string.substring(i + 1, size + i + 1);
                    for (User user : users) {
                        if (user.getName().equalsIgnoreCase(atPlayer)) {
                            string.replace(i, size + i + 1, user.getAsMention());
                            break getAt;
                        }
                        String nickName = Objects.requireNonNull(Objects.requireNonNull(DiscordBot.client.getTextChannelById(
                                DiscordBot.channelID)).getGuild().getMemberById(user.getId())).getEffectiveName();

                        if (nickName.equalsIgnoreCase(atPlayer)) {
                            string.replace(i, size + i + 1, user.getAsMention());
                            break getAt;
                        }
                    }
                }

            }
        }
        return string.toString();

    }
}