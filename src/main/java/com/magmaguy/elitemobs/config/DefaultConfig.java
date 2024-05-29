package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.commands.admin.ReloadCommand;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class DefaultConfig {
    @Getter
    private static boolean alwaysShowNametags;
    @Getter
    private static int superMobStackAmount;
    @Getter
    private static boolean preventCreeperDamageToPassiveMobs;
    @Getter
    private static boolean doPermissionTitles;
    @Getter
    private static boolean preventEliteMobConversionOfNamedMobs;
    @Getter
    private static boolean doStrictSpawningRules;
    @Getter
    private static double nightmareWorldSpawnBonus;
    @Getter
    private static boolean emLeadsToStatusMenu;
    @Getter
    private static boolean otherCommandsLeadToEMStatusMenu;
    @Getter
    private static boolean setupDone;
    @Getter
    private static Location defaultSpawnLocation;
    @Getter
    private static boolean doExplosionRegen;
    @Getter
    private static boolean preventVanillaReinforcementsForEliteEntities;
    @Getter
    private static boolean doRegenerateContainers;
    @Getter
    private static int defaultTransitiveBlockLimiter;
    @Getter
    private static boolean onlyUseBedrockMenus;
    @Getter
    private static int characterLimitForBookMenuPages;
    @Getter
    private static boolean useGlassToFillMenuEmptySpace;
    @Getter
    private static boolean menuUnicodeFormatting;
    @Getter
    private static String language;
    @Getter
    private static String noPendingCommands;
    @Getter
    private static String trackMessage;
    @Getter
    private static String chestLowRankMessage;
    @Getter
    private static String chestCooldownMessage;
    @Getter
    private static String dismissEMMessage;
    @Getter
    private static String switchEMStyleMessage;
    @Getter
    private static String treasureChestNoDropMessage;
    @Getter
    private static String bossAlreadyGoneMessage;

    private static File file = null;
    private static FileConfiguration fileConfiguration = null;

    private DefaultConfig() {
    }

    public static void toggleSetupDone() {
        setupDone = !setupDone;
        fileConfiguration.set("setupDoneV3", setupDone);
        save();
    }

    public static void save() {
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }


    public static void initializeConfig() {

        file = ConfigurationEngine.fileCreator("config.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        //The language changes what data is loaded downstream
        language = ConfigurationEngine.setString(
                List.of("Sets the language file used by EliteMobs", "Do NOT change this manually! It is meant to be installed with the command '/em language <languagefile>'"),
                file, fileConfiguration, "language", "english", false);
        //This needs to be initialized as soon as the language is figured out
        new TranslationsConfig();

        alwaysShowNametags = ConfigurationEngine.setBoolean(
                List.of("Sets whether elites and bosses spawned by elitemobs will always show their nametags.", "Not recommended!"),
                fileConfiguration, "alwaysShowEliteMobNameTags", false);
        superMobStackAmount = Math.max(ConfigurationEngine.setInt(
                List.of("Sets the amount of passive mobs that have to be in close proximity before they can be merged.", "Don't set this to 0!"),
                fileConfiguration, "superMobStackAmount", 50), 1);
        preventCreeperDamageToPassiveMobs = ConfigurationEngine.setBoolean(
                List.of("Makes elites not damage passive mobs with explosions."),
                fileConfiguration, "preventEliteCreeperDamageToPassiveMobs", true);
        doPermissionTitles = ConfigurationEngine.setBoolean(
                List.of("Sets whether EliteMobs will use titles to warn players about missing permissions"),
                fileConfiguration, "useTitlesForMissingPermissionMessages", true);
        preventEliteMobConversionOfNamedMobs = ConfigurationEngine.setBoolean(
                List.of("Sets whether EliteMobs will prevent converting named mobs to elites.", "Especially important for compatibility with other plugins."),
                fileConfiguration, "preventEliteMobConversionOfNamedMobs", true);
        doStrictSpawningRules = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will convert mobs with custom spawn reasons.", "Running certain boss plugins will automatically set this to true, no matter what the value in the config is."),
                fileConfiguration, "enableHighCompatibilityMode", false);
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs") ||
                Bukkit.getPluginManager().isPluginEnabled("LevelledMobs")) {
            new InfoMessage("Other boss mob plugins have been detected, high compatibility mode will be used!");
            doStrictSpawningRules = true;
        }
        nightmareWorldSpawnBonus = ConfigurationEngine.setDouble(
                List.of("Sets the elite spawn bonus for the nightmare gamemode"),
                fileConfiguration, "nightmareWorldSpawnBonus", 0.5);
        emLeadsToStatusMenu = ConfigurationEngine.setBoolean(
                List.of("Sets if the centralized /em command opens the main status page of the plugin.", "Highly recommended!"),
                fileConfiguration, "emLeadsToStatusMenu", true);
        otherCommandsLeadToEMStatusMenu = ConfigurationEngine.setBoolean(
                List.of("Sets if running specific commands like /em wallet will lead to the /em menu where that information is centralized."),
                fileConfiguration, "otherCommandsLeadToEMStatusMenu", true);
        setupDone = ConfigurationEngine.setBoolean(
                List.of("Sets if the setup is complete.", "Do not set this value manually, it is meant to be modified through in-game commands."),
                fileConfiguration, "setupDoneV3", false);
        preventVanillaReinforcementsForEliteEntities = ConfigurationEngine.setBoolean(
                List.of("Sets if elites will prevent spawning vanilla reinforcements, such as for the Zombie reinforcement feature."),
                fileConfiguration, "preventVanillaReinforcementsForEliteEntities", true);
        try {
            defaultSpawnLocation = ConfigurationLocation.serialize(
                    ConfigurationEngine.setString(
                            List.of("Sets the default spawn location of the server for EliteMobs. /em spawntp will lead to this location."),
                            file, fileConfiguration, "defaultSpawnLocation",
                            ConfigurationLocation.deserialize(Bukkit.getWorlds().get(0).getSpawnLocation()), false));
        } catch (Exception ex) {
            new WarningMessage("There is an issue with your defaultSpawnLocation in the config.yml configuration file! Fix it!");
        }

        doExplosionRegen = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will regenerate blocks blown up by elites."),
                fileConfiguration, "doExplosionRegen", true);
        doRegenerateContainers = ConfigurationEngine.setBoolean(
                List.of("Sets if the explosion regen will also regenerate the contents of containers such as chests.", "Turning it to false will make elite explosions not blow up containers."),
                fileConfiguration, "doRegenerateContainers", true);
        defaultTransitiveBlockLimiter = ConfigurationEngine.setInt(fileConfiguration, "defaultTransitiveBlockLimiter", 500);
        onlyUseBedrockMenus = ConfigurationEngine.setBoolean(
                List.of("Sets whether the /em menu will only use the inventory-based menu style which is compatible with bedrock.", "As a reminder, players can otherwise do the command /em alt to switch between /em menu styles"),
                fileConfiguration, "onlyUseBedrockMenus", false);
        characterLimitForBookMenuPages = ConfigurationEngine.setInt(
                List.of("Sets the character limit per line for book menu pages.", "Lower this amount if text is getting cut off in book menus such as for quests"),
                fileConfiguration, "characterLimitForBookMenuPagesV2", 170);
        useGlassToFillMenuEmptySpace = ConfigurationEngine.setBoolean(
                List.of("Sets if empty menu space will be filled with glass panes.", "Not recommended if you are using the EliteMobs resource pack."),
                fileConfiguration, "useGlassToFillMenuEmptySpace", false);
        menuUnicodeFormatting = ConfigurationEngine.setBoolean(
                List.of("Sets if unicode will be used to format the EliteMobs resource pack.",
                        "Do not set this manually, it is set automatically upon installation of the resource pack.",
                        "Only set it manually if you had to merge the EliteMobs resource pack, and expect that the spacing might not work if you do that."),
                fileConfiguration, "menuUnicodeFormatting", false);
        noPendingCommands = ConfigurationEngine.setString(
                List.of("Sets the message sent to players if they run '/em confirm' with no pending commands."),
                file, fileConfiguration, "noPendingCommands", "&cYou don't currently have any pending commands!", true);
        trackMessage = ConfigurationEngine.setString(
                List.of("Sets the tracking message for bosses that send tracking messages."),
                file, fileConfiguration, "trackMessage", "Track the $name", true);
        chestLowRankMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players that open treasure chests with level requirements above their guild rank."),
                file, fileConfiguration, "chestLowRankMessage", "&7[EM] &cYour guild rank needs to be at least $rank &cin order to open this chest!", true);
        chestCooldownMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players that try to reopen a treasure chest they have already opened."),
                file, fileConfiguration, "chestCooldownMessage", "&7[EM] &cYou've already opened this chest recently! Wait $time!", true);
        dismissEMMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears recommending the /em alt command for players having issues with the /em command"),
                file, fileConfiguration, "dismissEMMessage", "&8[EliteMobs] &2/elitemobs &fmenu not working for you? Try &2/elitemobs alt &fto see an alternative version of the menu! &cDon't want to see this message again? &4/em dismiss", true);
        switchEMStyleMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when players run the /em alt command."),
                file, fileConfiguration, "switchEMStyleMessage", "&8[EliteMobs] &2/elitemobs &fmenu style changed! Check it out!", true);
        treasureChestNoDropMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when a player opens a treasure chest but gets nothing"),
                file, fileConfiguration, "treasureChestNoDropMessage", "&8[EliteMobs] &cYou didn't get anything! Better luck next time!", true);
        bossAlreadyGoneMessage= ConfigurationEngine.setString(
                List.of("Sets the message that appears when a player tries to track a boss that is no longer valid"),
                file, fileConfiguration, "bossAlreadyGoneMessage", "&c[EliteMobs] Sorry, this boss is already gone!", true);



        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    public static void setLanguage(CommandSender commandSender, String filename) {
        language = filename;
        fileConfiguration.set("language", filename);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
        ReloadCommand.reload(commandSender);
    }

}
