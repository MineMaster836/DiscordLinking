package me.discordlinking.state;

import me.discordlinking.DiscordBot;

public enum ChatLinkingPolicy {
    ALL(true, true, "All messages"),
    DISCORD_TO_MINECRAFT(false, true, "Only discord to minecraft"),
    MINECRAFT_TO_DISCORD(true, false, "Only minecraft to discord"),
    NONE(false, false, "No messages");

    private final boolean isToDiscord;
    private final boolean isToMinecraft;
    private final String prettyName;

    ChatLinkingPolicy(boolean isToDiscord, boolean isToMinecraft, String prettyName) {
        this.isToDiscord = isToDiscord;
        this.isToMinecraft = isToMinecraft;
        this.prettyName = prettyName;
    }

    public boolean isToDiscord() {
        return isToDiscord && DiscordBot.channelID > 0;
    }

    public boolean isToMinecraft() {
        return isToMinecraft;
    }

    public String pretty() {
        return prettyName;
    }
}
