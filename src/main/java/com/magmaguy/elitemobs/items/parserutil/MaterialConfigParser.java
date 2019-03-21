package com.magmaguy.elitemobs.items.parserutil;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class MaterialConfigParser {

    public static Material parseMaterial(Configuration configuration, String previousPath) {
        String path = automatedStringBuilder(previousPath, "Type");
        return Material.getMaterial(configuration.getString(path));
    }

}
