package me.discordlinking.utils;

public class PrettyStrings {
    public static String uppercaseWords(String s) {
        String[] split = s.split(" ");
        for (int i = 0; i < split.length; i++) {
            split[i] = uppercaseFirstWord(split[i]);
        }
        return String.join(" ", split);
    }

    public static String uppercaseFirstWord(String s) {
        char[] chars = s.toCharArray();
        if (chars.length != 0)
            chars[0] = Character.toUpperCase(chars[0]);
        for (int i = 1; i < chars.length; i++) {
            chars[i] = Character.toLowerCase(chars[i]);
        }
        return new String(chars);
    }
}
