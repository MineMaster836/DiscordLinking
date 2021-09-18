package me.discordlinking.utils;

import me.discordlinking.Main;

import java.io.*;
import java.util.logging.Level;

public class WriteConfigFile {
    public static void run(File outputFile, String inputResource) {
        if (!outputFile.exists()) {
            InputStream messageFormatExample = Main.get().getResource(inputResource);
            if (messageFormatExample == null) {
                Main.log(Level.WARNING, "The example template for messageFormatting.yml does not exist, and neither does messageFormatting.yml");
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(messageFormatExample))) {
                    //noinspection ResultOfMethodCallIgnored
                    outputFile.getParentFile().mkdirs();
                    //noinspection ResultOfMethodCallIgnored
                    outputFile.createNewFile();
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
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
    }
}
