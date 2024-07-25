package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ValidWorldsConfig extends ConfigurationFile {
    @Getter
    private static final List<String> validWorlds = new ArrayList<>();
    @Getter
    private static ValidWorldsConfig instance;

    public ValidWorldsConfig() {
        super("ValidWorlds.yml");
        instance = this;
    }

    public static void addWorld(String worldName) {
        if (instance.fileConfiguration.getKeys(true).contains("Valid worlds." + worldName)) return;

        ConfigurationEngine.setBoolean(
                List.of("Sets if elites will spawn in this world."),
                instance.fileConfiguration, "Valid worlds." + worldName, true);
        ConfigurationEngine.fileSaverOnlyDefaults(instance.fileConfiguration, instance.file);
        validWorlds.add(worldName);
    }

    @Override
    public void initializeValues() {

        for (World world : Bukkit.getWorlds())
            ConfigurationEngine.setBoolean(fileConfiguration, "Valid worlds." + world.getName(), true);

        ConfigurationSection validWorldsSection = fileConfiguration.getConfigurationSection("Valid worlds");

        for (String key : validWorldsSection.getKeys(false))
            if (validWorldsSection.getBoolean(key))
                validWorlds.add(key);
    }
}
