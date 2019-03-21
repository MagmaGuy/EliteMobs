package com.magmaguy.elitemobs.items.parserutil;

import org.bukkit.configuration.Configuration;

import java.util.List;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class LoreConfigParser {

    public static List<String> parseLore(Configuration configuration, String previousPath) {
        String path = automatedStringBuilder(previousPath, "Lore");
        return (List<String>) configuration.getList(path);
    }

}
