package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 05/05/2017.
 */
public class ConfigValues {

    public static Configuration translationConfig,
            eventsConfig;

    public static void initializeCachedConfigurations() {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();

        customConfigLoader = new CustomConfigLoader();
        translationConfig = customConfigLoader.getCustomConfig(TranslationConfig.CONFIG_NAME);

        customConfigLoader = new CustomConfigLoader();
        eventsConfig = customConfigLoader.getCustomConfig(EventsConfig.CONFIG_NAME);

    }

    public static void initializeConfigurations() {
        TranslationConfig translationConfig = new TranslationConfig();
        translationConfig.initializeConfig();

        EventsConfig eventsConfig = new EventsConfig();
        eventsConfig.initializeConfig();
    }

}