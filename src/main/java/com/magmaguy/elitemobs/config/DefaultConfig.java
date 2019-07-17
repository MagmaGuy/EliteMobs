package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class DefaultConfig {

    public static final String ALWAYS_SHOW_NAMETAGS = "Always show Elite Mob name tags";
    public static final String SUPERMOB_STACK_AMOUNT = "SuperMob (passive EliteMobs) stack amount";
    public static final String MMORPG_COLORS = "Use MMORPG colors for item ranks";
    public static final String CREEPER_PASSIVE_DAMAGE_PREVENTER = "Prevent creepers from killing passive mobs";
    public static final String ENABLE_PERMISSION_TITLES = "Use titles to warn players they are missing a permission";
    public static final String HIDE_ENCHANTMENTS_ATTRIBUTE = "Hide enchantment attributes on plugin-generated items";
    public static final String PREVENT_ELITE_MOB_CONVERSION_OF_NAMED_MOBS = "Make aggressive named mobs unable to become Elite Mobs";
    public static final String STRICT_SPAWNING_RULES = "Strict spawning rules mode for better compatibility with other plugins";
    public static FileConfiguration fileConfiguration;

    public void loadConfiguration() {

        FileConfiguration configuration = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig();

        configuration.addDefault(SUPERMOB_STACK_AMOUNT, 50);
        configuration.addDefault(MMORPG_COLORS, true);
        configuration.addDefault(CREEPER_PASSIVE_DAMAGE_PREVENTER, true);
        configuration.addDefault(ENABLE_PERMISSION_TITLES, true);
        configuration.addDefault(ALWAYS_SHOW_NAMETAGS, false);
        configuration.addDefault(HIDE_ENCHANTMENTS_ATTRIBUTE, false);
        configuration.addDefault(PREVENT_ELITE_MOB_CONVERSION_OF_NAMED_MOBS, true);
        configuration.addDefault(STRICT_SPAWNING_RULES, false);

        configuration.options().copyDefaults(true);

        UnusedNodeHandler.clearNodes(configuration);

        //save the config when changed
        Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).saveConfig();
        Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).saveDefaultConfig();

        Bukkit.getLogger().info("EliteMobs config loaded!");

        fileConfiguration = configuration;

    }

}
