package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class AdventurersGuildConfig {
    @Getter
    private static final String[][] rankNames = new String[11][21];
    @Getter
    private static final String[][] shortRankNames = new String[11][21];
    @Getter
    private static boolean addMaxHealth;
    @Getter
    private static boolean addCrit;
    @Getter
    private static boolean addDodge;
    @Getter
    private static boolean guildWorldIsEnabled;
    @Getter
    private static String guildWorldName;
    @Getter
    private static String guildLocationString;
    @Getter
    @Setter
    private static Location guildWorldLocation;
    @Getter
    private static boolean agTeleport;
    @Getter
    private static boolean guildLootLimiter;
    @Getter
    private static String lootLimiterMessage;
    @Getter
    private static List<String> onRankUpCommand;
    @Getter
    private static List<String> onPrestigeUpCommand;
    @Getter
    private static double dodge1;
    @Getter
    private static double dodge2;
    @Getter
    private static double dodge3;
    @Getter
    private static double crit1;
    @Getter
    private static double crit2;
    @Getter
    private static double crit3;
    @Getter
    private static double health1;
    @Getter
    private static double health2;
    @Getter
    private static double health3;
    @Getter
    private static double health4;
    @Getter
    private static String adventurersGuildMenuName;
    @Getter
    private static int baseKillsForRankUp;
    @Getter
    private static int additionalKillsForRankUpPerTier;
    private static File file;
    private static FileConfiguration fileConfiguration;
    private AdventurersGuildConfig() {
    }

    public static void toggleGuildInstall() {
        guildWorldIsEnabled = !guildWorldIsEnabled;
        fileConfiguration.set("guildHubIsEnabledv2", guildWorldIsEnabled);
        save();
    }

    public static void save() {
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("AdventurersGuild.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        addMaxHealth = ConfigurationEngine.setBoolean(fileConfiguration, "Add max health when unlocking higher guild ranks", true);
        addCrit = ConfigurationEngine.setBoolean(fileConfiguration, "Add critical chance when unlocking higher guild ranks", true);
        addDodge = ConfigurationEngine.setBoolean(fileConfiguration, "Add dodge chance when unlocking higher guild ranks", true);
        guildWorldIsEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "guildHubIsEnabledv2", false);
        guildWorldName = ConfigurationEngine.setString(fileConfiguration, "Adventurer's Guild world name v3", "em_adventurers_guild");
        guildLocationString = ConfigurationEngine.setString(fileConfiguration, "Guild world coordinates", "208.5,88,236.5,-80,0");
        guildWorldLocation = null;
        agTeleport = ConfigurationEngine.setBoolean(fileConfiguration, "userCommandsTeleportToAdventurersGuild", true);
        adventurersGuildMenuName = ConfigurationEngine.setString(fileConfiguration, "adventurersGuildMenuName", "&6&lAdventurer's Hub");

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
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &8Commoner");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 1:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fRookie");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fRookie");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 2:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fNovice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fNovice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 3:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fApprentice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fApprentice");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 4:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Adventurer");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Adventurer");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 5:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Journeyman");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Journeyman");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 6:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Adept");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Adept");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 7:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&1Veteran");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &1Veteran");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 8:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&1Elite");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &1Elite");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 9:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&lMaster");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Master");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 10:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&5Hero");
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank));
                        } else {
                            shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
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
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 12:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Myth");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 13:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Immortal");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 14:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Chosen");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 15:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Ascendant");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 16:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Titan");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 17:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Demigod");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 18:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Deity");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 19:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Godhunter");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 20:
                        rankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Godslayer");
                        shortRankName = ConfigurationEngine.setString(fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank));
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + normalRank);
                }

            }

        guildLootLimiter = ConfigurationEngine.setBoolean(fileConfiguration, "limitLootBasedOnGuildTier", true);
        lootLimiterMessage = ConfigurationEngine.setString(fileConfiguration, "lootLimiterMessage", "&7[EM] &cYou must unlock the next guild rank through /ag to loot better items!");
        onRankUpCommand = ConfigurationEngine.setList(fileConfiguration, "onRankUpCommand", Collections.emptyList());
        onPrestigeUpCommand = ConfigurationEngine.setList(fileConfiguration, "onPrestigeUpCommand", Collections.emptyList());
        dodge1 = ConfigurationEngine.setDouble(fileConfiguration, "dodgePrestige3Bonus", 3);
        dodge2 = ConfigurationEngine.setDouble(fileConfiguration, "dodgePrestige6Bonus", 6);
        dodge3 = ConfigurationEngine.setDouble(fileConfiguration, "dodgePrestige9Bonus", 10);
        crit1 = ConfigurationEngine.setDouble(fileConfiguration, "critPrestige2Bonus", 3);
        crit2 = ConfigurationEngine.setDouble(fileConfiguration, "critPrestige5Bonus", 6);
        crit3 = ConfigurationEngine.setDouble(fileConfiguration, "critPrestige8Bonus", 10);
        health1 = ConfigurationEngine.setDouble(fileConfiguration, "healthPrestige1Bonus", 2);
        health2 = ConfigurationEngine.setDouble(fileConfiguration, "healthPrestige4Bonus", 2.5);
        health3 = ConfigurationEngine.setDouble(fileConfiguration, "healthPrestige7Bonus", 3);
        health4 = ConfigurationEngine.setDouble(fileConfiguration, "healthPrestige10Bonus", 4);

        baseKillsForRankUp = ConfigurationEngine.setInt(fileConfiguration, "baseKillsForRankUp", 100);
        additionalKillsForRankUpPerTier = ConfigurationEngine.setInt(fileConfiguration, "additionalKillsForRankUpPerTier", 50);

        //initializes the AG location
        AdventurersGuildCommand.defineTeleportLocation();

        save();
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
                return "&e";
            case 2:
                return "&2";
            case 3:
                return "&a";
            case 4:
                return "&3";
            case 5:
                return "&b";
            case 6:
                return "&4";
            case 7:
                return "&c";
            case 8:
                return "&9";
            case 9:
                return "&d";
            case 10:
                return "&5";
            default:
                return "error";
        }
    }

    private static String romanNumerals(int prestigeTier) {
        switch (prestigeTier) {
            case 1:
                return "Ⅰ";
            case 2:
                return "ⅠⅠ";
            case 3:
                return "ⅠⅠⅠ";
            case 4:
                return "ⅠⅤ";
            case 5:
                return "Ⅴ";
            case 6:
                return "ⅤⅠ";
            case 7:
                return "ⅤⅠⅠ";
            case 8:
                return "ⅤⅠⅠⅠ";
            case 9:
                return "ⅠⅩ";
            case 10:
                return "Ⅹ";
            case 11:
                return "ⅩⅠ";
            case 12:
                return "ⅩⅠⅠ";
            case 13:
                return "ⅩⅠⅠⅠ";
            case 14:
                return "ⅩⅠⅤ";
            case 15:
                return "ⅩⅤ";
            case 16:
                return "ⅩⅤⅠ";
            case 17:
                return "ⅩⅤⅠⅠ";
            case 18:
                return "ⅩⅤⅠⅠⅠ";
            case 19:
                return "ⅩⅠⅩ";
            case 20:
                return "ⅩⅩ";
            case 0:
                return "0";
            default:
                return "error";
        }
    }

}
