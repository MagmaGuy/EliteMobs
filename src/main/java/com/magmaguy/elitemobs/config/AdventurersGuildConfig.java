package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class AdventurersGuildConfig {

    public static boolean enableAdventurersGuild;
    public static boolean addMaxHealth;
    public static String guildWorldName;
    public static String guildLocationString;
    public static Location guildWorldLocation;
    public static boolean agTeleport;
    public static String[][] rankNames = new String[11][21];
    public static String[][] shortRankNames = new String[11][21];
    public static boolean guildLootLimiter;
    public static String lootLimiterMessage;
    public static boolean alwaysUseNpcs;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("AdventurersGuild.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        enableAdventurersGuild = ConfigurationEngine.setBoolean(fileConfiguration, "Enable adventurer's guild", true);
        addMaxHealth = ConfigurationEngine.setBoolean(fileConfiguration, "Add max health when unlocking higher guild ranks", true);
        guildWorldName = ConfigurationEngine.setString(fileConfiguration, "Adventurer's Guild world name", "EliteMobs_adventurers_guild");
        guildLocationString = ConfigurationEngine.setString(fileConfiguration, "Guild world coordinates", "208.5,88,236.5,-80,0");
        guildWorldLocation = null;
        agTeleport = ConfigurationEngine.setBoolean(fileConfiguration, "Teleport players to the adventurers guild using /ag", true);

        //iterate through all prestige tiers
        for (int prestigeRank = 0; prestigeRank < 11; prestigeRank++)
            for (int normalRank = 0; normalRank < 11 + prestigeRank; normalRank++) {
                String rankName = "";
                String shortRankName = "";
                switch (normalRank) {
                    case 0:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&8Commoner");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&80");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &8Commoner");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&80");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 1:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fRookie");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&8Ⅰ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fRookie");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&fⅠ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 2:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fNovice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&fⅡ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fNovice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&fⅡ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 3:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fApprentice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&fⅢ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fApprentice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&fⅢ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 4:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Adventurer");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&2Ⅳ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Adventurer");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&2Ⅳ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 5:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Journeyman");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&2Ⅴ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Journeyman");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&2Ⅴ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 6:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Adept");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&2Ⅵ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Adept");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&2Ⅵ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 7:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&1Veteran");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&1Ⅶ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &1Veteran");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&1Ⅶ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 8:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&1Elite");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&1Ⅷ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &1Elite");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&1Ⅷ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 9:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&lMaster");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&5Ⅸ");
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Master");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5Ⅸ");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 10:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&5Hero");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&5Ⅹ");
                        } else {
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5Ⅹ");
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Hero");
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 11:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Legend");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅠ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 12:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Myth");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅡ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 13:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Immortal");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅢ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 14:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Chosen");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅣ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 15:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Ascendant");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅤ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 16:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Titan");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅥ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 17:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Demigod");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅦ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 18:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Deity");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅧ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 19:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Godhunter");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅨ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 20:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Godslayer");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "P" + prestigeNumerals(prestigeRank) + "&5ⅩⅩ");
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                }

            }

        guildLootLimiter = ConfigurationEngine.setBoolean(fileConfiguration, "limitLootBasedOnGuildTier", true);
        lootLimiterMessage = ConfigurationEngine.setString(fileConfiguration, "lootLimiterMessage", "&7[EM] &cYou must unlock the next guild rank through /ag to loot better items!");
        alwaysUseNpcs = ConfigurationEngine.setBoolean(fileConfiguration, "alwaysUseNpcsWhenAvailable", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    public static String getRankName(int prestigeTier, int rankTier) {
        return ChatColorConverter.convert(rankNames[prestigeTier][rankTier]);
    }

    public static String getShortenedRankName(int prestigeTier, int rankTier) {
        return ChatColorConverter.convert(shortRankNames[prestigeTier][rankTier]);
    }

    private static String prestigeColors(int prestigeTier) {
        switch (prestigeTier) {
            case 1:
                return "&7";
            case 2:
            case 3:
                return "&f";
            case 4:
            case 5:
            case 6:
                return "&2";
            case 7:
            case 8:
            case 9:
                return "&1";
            case 10:
                return "&5";
            default:
                return "error";
        }
    }

    private static String prestigeNumerals(int prestigeTier) {
        switch (prestigeTier) {
            case 1:
                return "Ⅰ";
            case 2:
                return "Ⅱ";
            case 3:
                return "Ⅲ";
            case 4:
                return "Ⅳ";
            case 5:
                return "Ⅴ";
            case 6:
                return "Ⅵ";
            case 7:
                return "Ⅶ";
            case 8:
                return "Ⅷ";
            case 9:
                return "Ⅸ";
            case 10:
                return "Ⅹ";
            default:
                return "error";
        }
    }

}
