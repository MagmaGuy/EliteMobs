package com.magmaguy.elitemobs.items.parserutil;


import org.bukkit.configuration.Configuration;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class DropWeightConfigParser {

    public static String getDropType(Configuration configuration, String previousPath) {

        String dropType = automatedStringBuilder(previousPath, "Drop Weight");

        return configuration.getString(dropType);

    }

}
