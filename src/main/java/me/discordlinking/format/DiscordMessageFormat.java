package me.discordlinking.format;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.discordlinking.DiscordBot;
import me.discordlinking.Main;
import net.dv8tion.jda.api.OnlineStatus;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;

public class DiscordMessageFormat {
    private static String oldPresnce = null;

    static {
        File file = new File(Main.get().getDataFolder(), "messageFormatting.yml");
        if (!file.exists()) {
            InputStream messageFormatExample = Main.get().getResource("messageFormatExample.yml");
            if (messageFormatExample == null) {
                Main.log(Level.WARNING, "The example template for messageFormatting.yml does not exist, and neither does messageFormatting.yml");
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(messageFormatExample))) {
                    //noinspection ResultOfMethodCallIgnored
                    file.getParentFile().mkdirs();
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        int c;
                        while ((c = reader.read()) != -1) {
                            writer.write(c);
                        }
                    }
                } catch (IOException e) {
                    Main.log(Level.WARNING, "The example template for messageFormatting.yml could not be copied to messageFormatting.yml");
                }
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        All.username = config.getString("all.username");
        All.message = config.getString("all.message");
        Chat.Console.username = config.getString("chat.console.username");
        Chat.Console.message = config.getString("chat.console.message");
        Chat.Normal.username = config.getString("chat.normal.username");
        Chat.Normal.message = config.getString("chat.normal.message");
        Status.Misc.botname = config.getString("status.misc.botname");
        Status.Disable.username = config.getString("status.disable.username");
        Status.Disable.message = config.getString("status.disable.message");
        Status.Enable.username = config.getString("status.enable.username");
        Status.Enable.message = config.getString("status.enable.message");
        Status.Start.username = config.getString("status.start.username");
        Status.Start.message = config.getString("status.start.message");
        Status.ReloadStart.username = config.getString("status.reload_start.username");
        Status.ReloadStart.message = config.getString("status.reload_start.message");
        Status.Stop.username = config.getString("status.stop.username");
        Status.Stop.message = config.getString("status.stop.message");
        Status.ReloadStop.username = config.getString("status.reload_stop.username");
        Status.ReloadStop.message = config.getString("status.reload_stop.message");
        Login.username = config.getString("login.username");
        Login.message = config.getString("login.message");
        Logout.username = config.getString("logout.username");
        Logout.message = config.getString("logout.message");
        Death.username = config.getString("death.username");
        Death.message = config.getString("death.message");

        try {
            Activity.online_status = OnlineStatus.valueOf(config.getString("activity.online_status"));
        } catch (IllegalArgumentException | NullPointerException e) {
            Activity.online_status = OnlineStatus.ONLINE;
            Main.log(Level.WARNING, "Online status is not of a valid type");
        }
        try {
            Activity.activity = net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf(config.getString("activity.activity"));
        } catch (IllegalArgumentException | NullPointerException e) {
            Activity.activity = net.dv8tion.jda.api.entities.Activity.ActivityType.DEFAULT;
            Main.log(Level.WARNING, "Presence Activity is not of a valid type");
        }
        Activity.message = config.getString("activity.message");

    }

    /**
     * format the message with a player argument
     *
     * @param msg        the message to format
     * @param playerName the player supplied
     * @return the formatted message
     */
    private static String formatPlayer(String msg, String playerName) {
        return msg == null ? playerName : msg.replace("%player", playerName);
    }

    private static String formatMessage(String msg, String message) {
        return msg == null ? message : msg.replace("%string", message);
    }

    private static String formatMessagePlayer(String msg, String playerName, String message) {
        return msg == null ? message : msg.replace("%player", playerName)
                .replace("%string", message);
    }

    private static String formatAll(String msg) {
        Server server = Bukkit.getServer();
        return msg == null ? "" : msg
                .replace("%count", String.valueOf(server.getOnlinePlayers().size()))
                .replace("%max", String.valueOf(server.getMaxPlayers()))
                .replace("%servername", server.getName())
                .replace("%serverip", server.getIp());
    }

    private static String formatAllUsername(String formatting, String msg) {
        return msg == null ? "" : formatting.replace("%username", msg);
    }

    private static String formatAllMessage(String formatting, String msg) {
        return msg == null ? "" : formatting.replace("%message", msg);
    }

    public static void sendMessage(String username, String content) {
        sendMessage(username, DiscordBot.avatarURL, content);
    }

    public static void sendMessage(String username, String avatarURL, String content) {
        username = formatAll(username);
        content = formatAll(content);
        username = All.username(username);
        content = All.message(content);
        if (username.isBlank() || content.isBlank() || DiscordBot.webhookURL == null) return;
        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(username);
        if (avatarURL != null)
            builder.setAvatarUrl(avatarURL);
        builder.setContent(content);
        client.send(builder.build());
        client.close();
        verifyPresence();
    }

    private static void verifyPresence() {
        String newPresence = formatAll(Activity.message());
        if (!newPresence.equalsIgnoreCase(oldPresnce)) {
            setPresence(newPresence);
        }
    }

    public static void setPresence() {
        setPresence(formatAll(Activity.message()));
    }

    private static void setPresence(String newPresence) {
        oldPresnce = newPresence;
        if (newPresence != null && !newPresence.isEmpty()) {
            OnlineStatus status = Activity.online_status();
            net.dv8tion.jda.api.entities.Activity.ActivityType activity = Activity.activity();
            if (status != null && activity != null)
                DiscordBot.client.getPresence().setPresence(
                        status,
                        net.dv8tion.jda.api.entities.Activity.of(activity, newPresence));
        }
    }


    public static class Status {
        public static class Disable {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: [status.disable.username]
             * - message: format([status.disable.message], [playername])
             *
             * @param playerName the player that sent the message
             * @return the author that will be used when DiscordLinking is disabled
             * status.disable.username
             */
            public static String username(String playerName) {
                return formatPlayer(username, playerName);
            }

            /**
             * example:
             * - author: [status.disable.username]
             * - message: format([status.disable.message], [playername])
             *
             * @param playerName the player that sent the message
             * @return the message that will be used when DiscordLinking is disabled
             * status.disable.message
             */
            public static String message(String playerName) {
                return formatPlayer(message, playerName);
            }
        }

        public static class Enable {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: [status.enable.username]
             * - message: format([status.enable.message], [playername])
             *
             * @param playerName the player that sent the message
             * @return the author that will be used when DiscordLinking is enabled
             * status.enable.username
             */
            public static String username(String playerName) {
                return formatPlayer(username, playerName);
            }

            /**
             * example:
             * - author: [status.enable.username]
             * - message: format([status.enable.message], [playername])
             *
             * @param playerName the player that sent the message
             * @return the message that will be used when DiscordLinking is enabled
             * status.enable.message
             */
            public static String message(String playerName) {
                return formatPlayer(message, playerName);
            }
        }

        public static class Misc {
            private static String botname;

            /**
             * status.misc.botname
             * <p>
             * example:
             * [status.misc.botname] [status.disable/enable.message]
             *
             * @return the name that will be used when Console disables DiscordLinking
             * status.misc.botname
             */
            public static String botname() {
                return botname;
            }
        }

        public static class Start {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: [status.started.username]
             * - message: [status.started.message]
             *
             * @return the author that will be used when DiscordLinking is started
             * status.started.username
             */
            public static String username() {
                return username;
            }

            /**
             * example:
             * - author: [status.started.username]
             * - message: [status.started.message]
             *
             * @return the message that will be used when DiscordLinking is started
             * status.started.message
             */
            public static String message() {
                return message;
            }
        }

        public static class ReloadStart {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: [status.reload.username]
             * - message: format([status.reload.message], [playername])
             *
             * @return the author that will be used when DiscordLinking is reloaded
             * status.reload.username
             */
            public static String username() {
                return username;
            }

            /**
             * example:
             * - author: [status.reload.username]
             * - message: format([status.reload.message], [playername])
             *
             * @return the message that will be used when DiscordLinking is reloaded
             * status.reload.message
             */
            public static String message() {
                return message;
            }

        }

        public static class Stop {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: [status.stoped.username]
             * - message: [status.stoped.message]
             *
             * @return the author that will be used when DiscordLinking is stopedd
             * status.stop.username
             */
            public static String username() {
                return username;
            }

            /**
             * example:
             * - author: [status.stoped.username]
             * - message: [status.stoped.message]
             *
             * @return the message that will be used when DiscordLinking is stopedd
             * status.stop.message
             */
            public static String message() {
                return message;
            }
        }

        public static class ReloadStop {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: [status.reload_stop.username]
             * - message: [status.reload_stop.message]
             *
             * @return the author that will be used when DiscordLinking is reload_stoped
             * status.reload_stop.username
             */
            public static String username() {
                return username;
            }

            /**
             * example:
             * - author: [status.reload_stop.username]
             * - message: [status.reload_stop.message]
             *
             * @return the message that will be used when DiscordLinking is reload_stoped
             * status.reload_stop.message
             */
            public static String message() {
                return message;
            }
        }

    }

    public static class Login {
        public static String username;
        public static String message;

        /**
         * example:
         * - author: format([login.username], [in-game-message], [playername])
         * - message: format([login.message], [in-game-message], [playername])
         *
         * @param msg        the message that was sent in game
         * @param playerName the player that sent the message
         * @return the author that will be used when a player logs in
         * login.username
         */
        public static String username(String msg, String playerName) {
            return formatMessagePlayer(username, playerName, msg);
        }

        /**
         * example:
         * - author: format([login.username], [in-game-message], [playername])
         * - message: format([login.message], [in-game-message], [playername])
         *
         * @param msg        the message that was sent in game
         * @param playerName the player that sent the message
         * @return the message that will be used when a player logs in
         * login.message
         */
        public static String message(String msg, String playerName) {
            return formatMessagePlayer(message, playerName, msg);
        }
    }

    public static class Logout {
        private static String username;
        private static String message;

        /**
         * example:
         * - author: format([logout.username], [in-game-message], [playername])
         * - message: format([logout.message], [in-game-message], [playername])
         *
         * @param msg        the message that was sent in game
         * @param playerName the player that logged out
         * @return the author that will be used when a player logs in
         * logout.username
         */
        public static String username(String msg, String playerName) {
            return formatMessagePlayer(username, playerName, msg);
        }

        /**
         * example:
         * - author: format([logout.username], [in-game-message], [playername])
         * - message: format([logout.message], [in-game-message], [playername])
         *
         * @param msg        the message that was sent in game
         * @param playerName the player that logged out
         * @return the message that will be used when a player logs in
         * logout.message
         */
        public static String message(String msg, String playerName) {
            return formatMessagePlayer(message, playerName, msg);
        }
    }

    public static class Death {
        private static String username;
        private static String message;

        /**
         * example:
         * - author: format([death.username], [in-game-message], [playername])
         * - message: format([death.message], [in-game-message], [playername])
         *
         * @param msg        the message that was sent in game
         * @param playerName the player that died
         * @return the author that will be used when a player logs in
         * death.username
         */
        public static String username(String msg, String playerName) {
            return formatMessagePlayer(username, playerName, msg);
        }

        /**
         * example:
         * - author: format([death.username], [in-game-message], [playername])
         * - message: format([death.message], [in-game-message], [playername])
         *
         * @param msg        the message that was sent in game
         * @param playerName the player that died
         * @return the message that will be used when a player logs in
         * death.message
         */
        public static String message(String msg, String playerName) {
            return formatMessagePlayer(message, playerName, msg);
        }
    }

    public static class Activity {
        public static OnlineStatus online_status;
        public static net.dv8tion.jda.api.entities.Activity.ActivityType activity;
        public static String message;

        public static OnlineStatus online_status() {
            return online_status;
        }

        public static net.dv8tion.jda.api.entities.Activity.ActivityType activity() {
            return activity;
        }

        public static String message() {
            return message;
        }
    }

    public static class Chat {
        public static class Console {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: format([chat.console.username], [in-game-message])
             * - message: format([chat.console.message], [in-game-message])
             *
             * @param msg the message that was sent in game
             * @return the username that will be used when console talks
             * chat.console.username
             */
            public static String username(String msg) {
                return formatMessage(username, msg);
            }

            /**
             * example:
             * - author: format([chat.console.username], [in-game-message])
             * - message: format([chat.console.message], [in-game-message])
             *
             * @param msg the message that was sent in game
             * @return the message that will be used when console talks
             * chat.console.message
             */
            public static String message(String msg) {
                return formatMessage(message, msg);
            }
        }

        public static class Normal {
            private static String username;
            private static String message;

            /**
             * example:
             * - author: format([chat.normal.username], [in-game-message], [playername])
             * - message: format([chat.normal.message], [in-game-message], [playername])
             *
             * @param msg        the message that was sent in game
             * @param playerName the player that sent the message
             * @return the username that will be used when players talk
             * chat.normal.username
             */
            public static String username(String msg, String playerName) {
                return formatMessagePlayer(username, playerName, msg);
            }

            /**
             * example:
             * - author: format([chat.normal.username], [in-game-message], [playername])
             * - message: format([chat.normal.message], [in-game-message], [playername])
             *
             * @param msg        the message that was sent in game
             * @param playerName the player that sent the message
             * @return the message that will be used when players talk
             * chat.normal.message
             */
            public static String message(String msg, String playerName) {
                return formatMessagePlayer(message, playerName, msg);
            }
        }
    }

    public static class All {
        private static String username;
        private static String message;

        /**
         * @param msg the formatted message
         * @return the final message to send to discord
         * all.username
         */
        public static String username(String msg) {
            return formatAllUsername(username, msg);
        }

        /**
         * @param msg the formatted message
         * @return the final message to send to discord
         * all.message
         */
        public static String message(String msg) {
            return formatAllMessage(message, msg);
        }
    }
}
