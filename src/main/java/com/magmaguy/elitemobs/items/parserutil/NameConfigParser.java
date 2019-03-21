package com.magmaguy.elitemobs.items.parserutil;

import org.bukkit.configuration.Configuration;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class NameConfigParser {

    public static String parseName(Configuration configuration, String previousPath) {
        String path = automatedStringBuilder(previousPath, "Name");
        return configuration.getString(path);
    }

}
