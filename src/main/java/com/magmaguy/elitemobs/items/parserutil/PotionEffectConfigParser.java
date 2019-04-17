package com.magmaguy.elitemobs.items.parserutil;

import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class PotionEffectConfigParser {

    public static List<String> itemPotionEffectHandler(Configuration configuration, String previousPath) {

        String path = automatedStringBuilder(previousPath, "Potion Effects");

        if (configuration.getList(path) == null || configuration.getList(path).isEmpty())
            return new ArrayList<>();

        List potionEffects = configuration.getList(path);

        return potionEffects;

    }

}
