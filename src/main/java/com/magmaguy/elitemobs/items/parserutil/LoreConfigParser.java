package com.magmaguy.elitemobs.items.parserutil;

import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class LoreConfigParser {

    public static List<String> parseLore(Configuration configuration, String previousPath) {
        String path = automatedStringBuilder(previousPath, "Item Lore");

        if (configuration.getList(path) == null || configuration.getList(path).isEmpty())
            return new ArrayList<>();

        return (List<String>) configuration.getList(path);
    }

}
