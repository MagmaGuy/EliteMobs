package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import com.magmaguy.magmacore.util.ChatColorConverter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdventurersGuildConfig extends ConfigurationFile {
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
    @Getter
    private static String prestigeUnlockMessageTitle;
    @Getter
    private static String prestigeUnlockMessageSubtitle;
    @Getter
    private static List<String> worldsWithoutAGBonuses;
    @Getter
    private static double peacefulModeEliteChanceDecrease;
    @Getter
    private static boolean disableCommonerRank;

    public AdventurersGuildConfig() {
        super("AdventurersGuild.yml");
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

    @Override
    public void initializeValues() {
        addMaxHealth = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will add max health when unlocking guild ranks as a prestige reward"),
                fileConfiguration, "Add max health when unlocking higher guild ranks", true);
        addCrit = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will add a critical strike chance when unlocking guild ranks as a prestige reward"),
                fileConfiguration, "Add critical chance when unlocking higher guild ranks", true);
        addDodge = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will add dodge chance when unlocking guild ranks as a prestige reward"),
                fileConfiguration, "Add dodge chance when unlocking higher guild ranks", true);
        agTeleport = ConfigurationEngine.setBoolean(
                List.of("Sets if user commands get rerouted to the adventurer's guild hub. This is highly recommended for gameplay immersion and tutorial purposes."),
                fileConfiguration, "userCommandsTeleportToAdventurersGuild", true);
        adventurersGuildMenuName = ConfigurationEngine.setString(
                List.of("Sets the in-game display name of the adventurer's guild"),
                file, fileConfiguration, "adventurersGuildMenuName", "&6&lAdventurer's Hub", true);

        //iterate through all prestige tiers
        for (int prestigeRank = 0; prestigeRank < 11; prestigeRank++)
            for (int normalRank = 0; normalRank < 11 + prestigeRank; normalRank++) {
                String rankName = "";
                String shortRankName = "";
                switch (normalRank) {
                    case 0:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&8Commoner - disables elites!", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &8Commoner", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 1:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fRookie", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fRookie", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 2:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fNovice", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fNovice", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 3:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&fApprentice", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &fApprentice", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 4:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Adventurer", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Adventurer", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 5:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Journeyman", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Journeyman", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 6:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&2Adept", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &2Adept", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 7:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&1Veteran", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &1Veteran", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 8:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&1Elite", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &1Elite", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 9:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&lMaster", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Master", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 10:
                        if (prestigeRank == 0) {
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, "&5Hero", true);
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", "&6&l✧&e" + romanNumerals(normalRank), true);
                        } else {
                            shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                            rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                    "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Hero", true);
                        }
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 11:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Legend", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 12:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Myth", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 13:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Immortal", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 14:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Chosen", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 15:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Ascendant", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 16:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Titan", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 17:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Demigod", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 18:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Deity", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 19:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Godhunter", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    case 20:
                        rankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank, prestigeColors(prestigeRank) + "Prestige " + prestigeRank + " &5Godslayer", true);
                        shortRankName = ConfigurationEngine.setString(file, fileConfiguration,
                                "Prestige " + prestigeRank + " rank " + normalRank + " short placeholder", prestigeColors(prestigeRank) + "⚜" + romanNumerals(prestigeRank) + "&6&l✧&e" + romanNumerals(normalRank), true);
                        rankNames[prestigeRank][normalRank] = rankName;
                        shortRankNames[prestigeRank][normalRank] = shortRankName;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + normalRank);
                }

            }

        guildLootLimiter = ConfigurationEngine.setBoolean(
                List.of("Sets if player loot is limited by their guild level.",
                        " This is an incredibly important part of EliteMobs and extremely highly recommended."),
                fileConfiguration, "limitLootBasedOnGuildTier", true);
        lootLimiterMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players if their loot gets nerfed due to their low guild level."),
                file, fileConfiguration, "lootLimiterMessage", "&7[EM] &cYou must unlock the next guild rank through /ag to loot better items!", true);
        onRankUpCommand = ConfigurationEngine.setList(
                List.of("Sets the commands that run on guild rank up. Placeholders are:", "$prestigerank - outputs the prestige rank", "$activerank - outputs the currently active rank", "$player - outputs the player name"),
                file, fileConfiguration, "onRankUpCommand", Collections.emptyList(), false);
        onPrestigeUpCommand = ConfigurationEngine.setList(
                List.of("Sets the commands that run on prestige rank up.", "$prestigerank - outputs the prestige rank", "$activerank - outputs the currently active rank", "$player - outputs the player name"),
                file, fileConfiguration, "onPrestigeUpCommand", Collections.emptyList(), false);
        dodge1 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the first dodge bonus."),
                fileConfiguration, "dodgePrestige3Bonus", 3);
        dodge2 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the second dodge bonus."),
                fileConfiguration, "dodgePrestige6Bonus", 6);
        dodge3 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the third dodge bonus."),
                fileConfiguration, "dodgePrestige9Bonus", 10);
        crit1 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the first critical hit bonus."),
                fileConfiguration, "critPrestige2Bonus", 3);
        crit2 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the second critical hit bonus."),
                fileConfiguration, "critPrestige5Bonus", 6);
        crit3 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the third critical hit bonus."),
                fileConfiguration, "critPrestige8Bonus", 10);
        health1 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the first max health bonus."),
                fileConfiguration, "healthPrestige1Bonus", 2);
        health2 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the second max health bonus."),
                fileConfiguration, "healthPrestige4Bonus", 2.5);
        health3 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the third max health bonus."),
                fileConfiguration, "healthPrestige7Bonus", 3);
        health4 = ConfigurationEngine.setDouble(
                List.of("Sets the prestige level for the third max health bonus."),
                fileConfiguration, "healthPrestige10Bonus", 4);

        baseKillsForRankUp = ConfigurationEngine.setInt(
                List.of("Sets the estimated base amount of bosses that must be killed to be able to afford a rank up."),
                fileConfiguration, "baseKillsForRankUpV2", 100);
        additionalKillsForRankUpPerTier = ConfigurationEngine.setInt(
                List.of("Sets the estimated additional amount of bosses that must be killed to be able to rank up, per level.",
                        "The formula is this amount x the level the player is currently at."),
                fileConfiguration, "additionalKillsForRankUpPerTierV2", 50);

        prestigeUnlockMessageTitle = ConfigurationEngine.setString(
                List.of("Sets title sent to players when someone unlocks a prestige rank.",
                        "$player is a placeholder that gets replaced with the player's display name."),
                file, fileConfiguration, "prestigeUnlockMessageTitle", "$player", true);
        prestigeUnlockMessageSubtitle = ConfigurationEngine.setString(
                List.of("Sets subtitle sent to players when someone unlocks a prestige rank.",
                        "$tier is a placeholder that gets replaced with the player's prestige level."),
                file, fileConfiguration, "prestigeUnlockMessageSubtitle", "&2has unlocked $tier&2!", true);

        peacefulModeEliteChanceDecrease = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier that will be applied to the spawn chance when players are using the peaceful (commoner) rank."),
                fileConfiguration, "peacefulModeEliteChanceDecrease", 0.2);

        worldsWithoutAGBonuses = ConfigurationEngine.setList(
                List.of("Sets the list of worlds to which guild rank bonuses will not be applied"),
                file, fileConfiguration, "worldsWithoutAGBonuses", new ArrayList(), false);

        disableCommonerRank = ConfigurationEngine.setBoolean(
                List.of("Disables users' ability to switch to peaceful mode for EliteMobs. Peaceful mode lowers level and spawn rates of mobs around that player specifically"),
                fileConfiguration, "disableCommonerRank", false);
    }
}
