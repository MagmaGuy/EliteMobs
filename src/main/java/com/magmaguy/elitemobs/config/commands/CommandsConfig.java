package com.magmaguy.elitemobs.config.commands;

import com.magmaguy.elitemobs.config.commands.premade.CheckTierConfig;
import com.magmaguy.elitemobs.config.commands.premade.CheckTierOthersConfig;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandsConfig {

    private static final ArrayList<CommandsConfigFields> commandsConfigFields = new ArrayList<>(Arrays.asList(
            new CheckTierConfig(),
            new CheckTierOthersConfig()
    ));

    public static void initializeConfigs() {
        //Checks if the directory doesn't exist
        for (CommandsConfigFields commandsConfigFields : commandsConfigFields)
            initializeConfiguration(commandsConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param commandsConfigFields
     * @return
     */
    private static FileConfiguration initializeConfiguration(CommandsConfigFields commandsConfigFields) {

        File file = ConfigurationEngine.fileCreator("commands", commandsConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        commandsConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

        return fileConfiguration;

    }

}
