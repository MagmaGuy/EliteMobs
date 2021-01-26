package com.magmaguy.elitemobs.config.events;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.events.premade.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EventsConfig {

    public static HashMap<String, EventsFieldConfig> eventFields = new HashMap<>();

    public static EventsFieldConfig getEventFields(String fileName) {
        return eventFields.get(fileName);
    }

    private static final ArrayList<EventsFieldConfig> eventsFieldConfigs = new ArrayList<>(Arrays.asList(
            new DeadMoonEventConfig(),
            new SmallTreasureGoblinEventConfig(),
            new BalrogEventConfig(),
            new KrakenEventConfig(),
            new FaeEventConfig(),
            new MeteorEventConfig()
    ));

    public static void initializeConfigs() {
        //Checks if the directory doesn't exist
        for (EventsFieldConfig eventsFieldConfig : eventsFieldConfigs)
            initializeConfiguration(eventsFieldConfig);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param eventsFieldConfig
     * @return
     */
    private static FileConfiguration initializeConfiguration(EventsFieldConfig eventsFieldConfig) {

        File file = ConfigurationEngine.fileCreator("events", eventsFieldConfig.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        eventsFieldConfig.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

        eventFields.put(file.getName(), new EventsFieldConfig(fileConfiguration, file));

        return fileConfiguration;

    }


}
