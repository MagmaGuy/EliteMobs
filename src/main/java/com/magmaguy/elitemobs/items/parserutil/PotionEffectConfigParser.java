package com.magmaguy.elitemobs.items.parserutil;

import org.bukkit.configuration.Configuration;

import java.util.List;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class PotionEffectConfigParser {

    public static List<String> itemPotionEffectHandler(Configuration configuration, String previousPath) {

        String path = automatedStringBuilder(previousPath, "Potion Effects");

        List potionEffects = configuration.getList(path);

        return potionEffects;

    }

}
