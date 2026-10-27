package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidWorldsConfig extends ConfigurationFile {
    @Getter
    private static final List<String> validWorlds = new ArrayList<>();
    @Getter
    private static ValidWorldsConfig instance;
    @Getter
    private static boolean disableEliteItemPowersInInvalidWorlds;
    // Worlds set to false; a world not recorded yet counts as valid, as it does for spawning.
    private static volatile Set<String> invalidWorlds = Set.of();

    public ValidWorldsConfig() {
        super("ValidWorlds.yml");
        instance = this;
    }

    public static void addWorld(String worldName) {
        if (instance.fileConfiguration.contains("validWorlds." + worldName)) return;

        ConfigurationEngine.setBoolean(
                List.of("Whether elites will spawn in this world."),
                instance.fileConfiguration, "validWorlds." + worldName, true);
        ConfigurationEngine.fileSaverOnlyDefaults(instance.fileConfiguration, instance.file);
        validWorlds.add(worldName);
    }

    /** Whether EliteMobs gear powers are off for this player: the option is on and their world is set to false. */
    public static boolean itemPowersDormant(Player player) {
        return disableEliteItemPowersInInvalidWorlds && invalidWorlds.contains(player.getWorld().getName());
    }

    @Override
    public void initializeValues() {
        disableEliteItemPowersInInvalidWorlds = ConfigurationEngine.setBoolean(
                List.of("When true, EliteMobs gear goes dormant in the worlds set to false below: its potion effects",
                        "stop, and its enchantment powers, item sets and staff or wand enchantments do not trigger.",
                        "Everything comes back in a world set to true. The gear's own damage and armor values still",
                        "apply, like any item's."),
                fileConfiguration, "disableEliteItemPowersInInvalidWorlds", false);

        for (World world : Bukkit.getWorlds())
            ConfigurationEngine.setBoolean(fileConfiguration, "validWorlds." + world.getName(), true);

        fileConfiguration.options().copyDefaults(true);
        ConfigurationSection validWorldsSection = fileConfiguration.getConfigurationSection("validWorlds");

        validWorlds.clear();
        invalidWorlds = Set.of();
        if (validWorldsSection == null) return;
        Set<String> invalid = new HashSet<>();
        for (String key : validWorldsSection.getKeys(false))
            if (validWorldsSection.getBoolean(key))
                validWorlds.add(key);
            else invalid.add(key);
        invalidWorlds = Set.copyOf(invalid);
    }
}
