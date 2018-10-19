package com.magmaguy.elitemobs.items.parserutil;


import org.bukkit.configuration.Configuration;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class ScalabilityConfigParser {

    public static String parseItemScalability(Configuration configuration, String previousPath) {

        String path = automatedStringBuilder(previousPath, "Scalability");

        return configuration.getString(path);

    }

}
