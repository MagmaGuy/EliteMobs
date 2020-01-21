package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class AdventurersGuildConfig {

    public static boolean enableAdventurersGuild;
    public static boolean addMaxHealth;
    public static String guildWorldName;
    public static Location guildWorldLocation;
    public static boolean agTeleport;
    private static String RANK_NAMES = "Adventurers Guild Rank Names";
    private static String guildRank = "Guild Rank ";
    public static String rankNames0 = guildRank + RANK_NAMES + "0";
    public static String rankNames1 = guildRank + RANK_NAMES + "1";
    public static String rankNames2 = guildRank + RANK_NAMES + "2";
    public static String rankNames3 = guildRank + RANK_NAMES + "3";
    public static String rankNames4 = guildRank + RANK_NAMES + "4";
    public static String rankNames5 = guildRank + RANK_NAMES + "5";
    public static String rankNames6 = guildRank + RANK_NAMES + "6";
    public static String rankNames7 = guildRank + RANK_NAMES + "7";
    public static String rankNames8 = guildRank + RANK_NAMES + "8";
    public static String rankNames9 = guildRank + RANK_NAMES + "9";
    public static String rankNames10 = guildRank + RANK_NAMES + "10";
    public static String rankNames11 = guildRank + RANK_NAMES + "11";
    public static boolean guildLootLimiter;
    public static String lootLimiterMessage;
    public static boolean alwaysUseNpcs;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("AdventurersGuild.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        enableAdventurersGuild = ConfigurationEngine.setBoolean(fileConfiguration, "Enable adventurer's guild", true);
        addMaxHealth = ConfigurationEngine.setBoolean(fileConfiguration, "Add max health when unlocking higher guild ranks", true);
        guildWorldName = ConfigurationEngine.setString(fileConfiguration, "Adventurer's Guild world name", "EliteMobs_adventurers_guild");
        String locationString = ConfigurationEngine.setString(fileConfiguration, "Guild world coordinates", "208.5,88,236.5,-80,0");

        new BukkitRunnable() {
            @Override
            public void run() {
                guildWorldLocation = null;

                for (World world : Bukkit.getWorlds())
                    if (world.getName().equals(AdventurersGuildConfig.guildWorldName)) {
                        double x = 0, y = 0, z = 0;
                        float yaw = 0, pitch = 0;
                        int counter = 0;

                        for (String substring : locationString.split(",")) {
                            switch (counter) {
                                case 0:
                                    x = Double.parseDouble(substring);
                                    break;
                                case 1:
                                    y = Double.parseDouble(substring);
                                    break;
                                case 2:
                                    z = Double.parseDouble(substring);
                                    break;
                                case 3:
                                    yaw = Float.parseFloat(substring);
                                    break;
                                case 4:
                                    pitch = Float.parseFloat(substring);
                                    break;
                            }
                            counter++;

                        }

                        guildWorldLocation = new Location(world, x, y, z, yaw, pitch);

                    }
            }
        }.runTaskLaterAsynchronously(MetadataHandler.PLUGIN, 20 * 10);

        agTeleport = ConfigurationEngine.setBoolean(fileConfiguration, "Teleport players to the adventurers guild using /ag", true);
        rankNames0 = ConfigurationEngine.setString(fileConfiguration, "0", "&8Commoner");
        rankNames1 = ConfigurationEngine.setString(fileConfiguration, "1", "&fFresh Meat");
        rankNames2 = ConfigurationEngine.setString(fileConfiguration, "2", "&fRookie");
        rankNames3 = ConfigurationEngine.setString(fileConfiguration, "3", "&fAdventurer");
        rankNames4 = ConfigurationEngine.setString(fileConfiguration, "4", "&2Journeyman");
        rankNames5 = ConfigurationEngine.setString(fileConfiguration, "5", "&2Elite Adventurer");
        rankNames6 = ConfigurationEngine.setString(fileConfiguration, "6", "&2Veteran");
        rankNames7 = ConfigurationEngine.setString(fileConfiguration, "7", "&1Slayer");
        rankNames8 = ConfigurationEngine.setString(fileConfiguration, "8", "&1Exterminator");
        rankNames9 = ConfigurationEngine.setString(fileConfiguration, "9", "&5&lElite Hunter");
        rankNames10 = ConfigurationEngine.setString(fileConfiguration, "10", "&5Hero");
        rankNames11 = ConfigurationEngine.setString(fileConfiguration, "11", "&6&l&oLegend");
        guildLootLimiter = ConfigurationEngine.setBoolean(fileConfiguration, "limitLootBasedOnGuildTier", true);
        lootLimiterMessage = ConfigurationEngine.setString(fileConfiguration, "lootLimiterMessage", "&7[EM] &cYou must unlock the next guild rank through /ag to loot better items!");
        alwaysUseNpcs = ConfigurationEngine.setBoolean(fileConfiguration, "alwaysUseNpcsWhenAvailable", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
