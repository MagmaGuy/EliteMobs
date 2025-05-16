package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.config.ConfigurationFile;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class DefaultConfig extends ConfigurationFile {
    @Getter
    private static boolean useResourcePackEvenIfResourcePackManagerIsNotInstalled;

    @Getter
    public static DefaultConfig instance;
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
    @Getter
    private static boolean resetPlayerScaleOnLogin;
    @Getter
    private static String enchantmentChallengeFailureMessage;
    @Getter
    private static String enchantmentChallengeSuccessMessage;
    @Getter
    private static String enchantmentChallengeInventoryFullMessage;
    @Getter
    private static String enchantmentChallengeConsequencesMessage;
    @Getter
    private static String enchantmentChallengeCriticalFailureMessage;
    @Getter
    private static String enchantmentChallengeStartMessage;

    public static boolean useResourcePackModels(){
        return DefaultConfig.useResourcePackEvenIfResourcePackManagerIsNotInstalled || Bukkit.getPluginManager().isPluginEnabled("ResourcePackManager");
    }

    public DefaultConfig() {
        super("config.yml");
        instance = this;
    }

    public static void toggleSetupDone() {
        setupDone = !setupDone;
        ConfigurationEngine.writeValue(setupDone, instance.file, instance.getFileConfiguration(), "setupDoneV4");
    }

    public static void toggleSetupDone(boolean value) {
        setupDone = value;
        ConfigurationEngine.writeValue(setupDone, instance.file, instance.getFileConfiguration(), "setupDoneV4");
    }


    public static void save() {
        ConfigurationEngine.fileSaverOnlyDefaults(instance.fileConfiguration, instance.file);
    }

    public static void setLanguage(CommandSender commandSender, String filename) {
        language = filename;
        instance.fileConfiguration.set("language", filename);
        ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
        ReloadCommand.reload(commandSender);
    }

    @Override
    public void initializeValues() {
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
            Logger.info("Other boss mob plugins have been detected, high compatibility mode will be used!");
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
                fileConfiguration, "setupDoneV4", false);
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
            Logger.warn("There is an issue with your defaultSpawnLocation in the config.yml configuration file! Fix it!");
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
        bossAlreadyGoneMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when a player tries to track a boss that is no longer valid"),
                file, fileConfiguration, "bossAlreadyGoneMessage", "&c[EliteMobs] Sorry, this boss is already gone!", true);
        resetPlayerScaleOnLogin = ConfigurationEngine.setBoolean(List.of("Sets whether to reset player scale (literally, the player size on login).", "This is important because some elite powers can modify it and if the server crashes players will be stuck to whatever scale was set when the server crashed, unless this option is set to true."), fileConfiguration, "resetPlayerScale", true);
        enchantmentChallengeFailureMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when they fail an enchantment challenge"),
                file, fileConfiguration, "enchantmentChallengeFailureMessage", "&8[EliteMobs] &cFailed enchantment! The enchantment did not bind to the item.", true);
        enchantmentChallengeSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when they succeed an enchantment challenge"),
                file, fileConfiguration, "enchantmentChallengeSuccessMessage", "&8[EliteMobs] &2Successful enchantment!", true);

        enchantmentChallengeInventoryFullMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when their inventory is full during an enchantment challenge"),
                file, fileConfiguration, "enchantmentChallengeInventoryFullMessage", "&8[EliteMobs] &cYour inventory was full so the item you were trying to upgrade has been deleted! It will be restored if your item is not full by the time you leave the instanced dungeon.", true);

        enchantmentChallengeConsequencesMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players about the consequences of an enchantment challenge"),
                file, fileConfiguration, "enchantmentChallengeConsequencesMessage", "&cThere's a 10% chance of losing your item if you lose the fight! Leaving the arena counts as losing.", true);

        enchantmentChallengeCriticalFailureMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when they critically fail an enchantment challenge"),
                file, fileConfiguration, "enchantmentChallengeCriticalFailureMessage", "&8[EliteMobs] &4Critical failure! You lost the item!", true);

        enchantmentChallengeStartMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players at the start of an enchantment challenge"),
                file, fileConfiguration, "enchantmentChallengeStartMessage", "&8[EliteMobs] &6Challenge! Defeat the boss to get your upgraded item!", true);
        useResourcePackEvenIfResourcePackManagerIsNotInstalled = ConfigurationEngine.setBoolean(
                List.of("Enables the use of resource packs even if the resource pack manager is not installed"), fileConfiguration, "useResourcePackEvenIfResourcePackManagerIsNotInstalled", false);
    }
}
