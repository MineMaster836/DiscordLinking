package me.discordlinking.reactions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface ReactableMessage {
    void dealWithReaction(MessageReactionAddEvent event);

    long getId();

    boolean isOld();
}
