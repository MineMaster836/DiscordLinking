package me.discordlinking.reactions;

import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.*;

public class MyReactionsKnowledge {
    public final static List<String> emojiAlphabet = Arrays.asList("\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED",
            "\uD83C\uDDEE", "\uD83C\uDDEF", "\uD83C\uDDF0", "\uD83C\uDDF1", "\uD83C\uDDF2", "\uD83C\uDDF3", "\uD83C\uDDF4", "\uD83C\uDDF5", "\uD83C\uDDF6", "\uD83C\uDDF7", "\uD83C\uDDF8", "\uD83C\uDDF9", "\uD83C\uDDFA"
            , "\uD83C\uDDFB", "\uD83C\uDDFC", "\uD83C\uDDFD", "\uD83C\uDDFE", "\uD83C\uDDFF");
    private static final Map<String, KnownReaction> emojis = new HashMap<>();

    public static int toAlphabetIndex(String emoji) {
        int i = 0;
        for (String e : emojiAlphabet) {
            if (e.equals(emoji)) return i;
            i++;
        }
        return -1;
    }


    public enum KnownReaction {
        LEFT("\u2B05"),
        RIGHT("\u27A1"),
        UNKNOWN_EMOJI,
        EMOJI_ALPHABET,
        CHECKMARK("\u2705");


        private String emoji;

        KnownReaction(String emoji) {
            emojis.put(emoji, this);
            this.emoji = emoji;
        }

        KnownReaction() {
        }

        public String getEmoji() {
            return emoji;
        }

        public static KnownReaction toReaction(MessageReaction.ReactionEmote reactionEmote) {
            String emojiString = reactionEmote.getEmoji();
            KnownReaction emoji = emojis.get(emojiString);
            if (emoji != null) return emoji;
            if (emojiAlphabet.contains(emojiString)) return EMOJI_ALPHABET;
            return UNKNOWN_EMOJI;
        }


    }
}
