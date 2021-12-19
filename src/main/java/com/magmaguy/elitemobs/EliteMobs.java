package com.magmaguy.elitemobs;

/*
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.config.commands.CommandsConfig;
import com.magmaguy.elitemobs.config.configurationimporter.ConfigurationImporter;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.economy.VaultCompatibility;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.ActionEvent;
import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.gamemodes.nightmaremodeworld.DaylightWatchdog;
import com.magmaguy.elitemobs.gamemodes.zoneworld.Grid;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobs.passive.EggRunnable;
import com.magmaguy.elitemobs.mobs.passive.PassiveEliteMobDeathHandler;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.powerstances.MajorPowerStanceMath;
import com.magmaguy.elitemobs.powerstances.MinorPowerStanceMath;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.thirdparty.bstats.CustomCharts;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.placeholderapi.Placeholders;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.ServerTime;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.elitemobs.versionnotifier.VersionWarner;
import com.magmaguy.elitemobs.worlds.CustomWorldLoading;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EliteMobs extends JavaPlugin {

    public static List<World> validWorldList = new ArrayList();
    public static boolean worldGuardIsEnabled = false;
    public static List<World> zoneBasedSpawningWorlds = new ArrayList<>();
    public static List<World> nightmareWorlds = new ArrayList<>();
    public static Metrics metrics;
    public Object placeholders = null;

    public static void initializeConfigs() {
        DefaultConfig.initializeConfig();
        ItemSettingsConfig.initializeConfig();
        ProceduralItemGenerationSettingsConfig.initializeConfig();
        PotionEffectsConfig.initializeConfigs();
        ConfigValues.initializeConfigurations();
        ConfigValues.initializeCachedConfigurations();
        EconomySettingsConfig.initializeConfig();
        new EnchantmentsConfig();
        AntiExploitConfig.initializeConfig();
        CombatTagConfig.initializeConfig();
        AntiExploitConfig.initializeConfig();
        AdventurersGuildConfig.initializeConfig();
        ValidWorldsConfig.initializeConfig();

        new MenusConfig();
        new PowersConfig();
        MobPropertiesConfig.initializeConfigs();
        CustomEnchantment.initializeCustomEnchantments();

        MobCombatSettingsConfig.initializeConfig();
        CommandsConfig.initializeConfigs();
        DiscordSRVConfig.initializeConfig();
        ReadMeForTranslationsConfig.initialize();
        ItemUpgradeSystemConfig.initializeConfig();
        new CustomEventsConfig();
        QuestsConfig.initializeConfig();
    }

    public static void worldScanner() {
        for (World world : Bukkit.getWorlds())
            if (ValidWorldsConfig.fileConfiguration.getBoolean("Valid worlds." + world.getName())) {
                validWorldList.add(world);
                if (ValidWorldsConfig.zoneBasedWorlds.contains(world.getName()))
                    zoneBasedSpawningWorlds.add(world);
                if (ValidWorldsConfig.nightmareWorlds.contains(world.getName())) {
                    nightmareWorlds.add(world);
                    DaylightWatchdog.preventDaylight(world);
                }
            }

    }

    @Override
    public void onEnable() {

        Bukkit.getLogger().info(" _____ _     _____ _____ ________  ______________  _____");
        Bukkit.getLogger().info("|  ___| |   |_   _|_   _|  ___|  \\/  |  _  | ___ \\/  ___|");
        Bukkit.getLogger().info("| |__ | |     | |   | | | |__ | .  . | | | | |_/ /\\ `--.");
        Bukkit.getLogger().info("|  __|| |     | |   | | |  __|| |\\/| | | | | ___ \\ `--. \\");
        Bukkit.getLogger().info("| |___| |_____| |_  | | | |___| |  | \\ \\_/ / |_/ //\\__/ /");
        Bukkit.getLogger().info("\\____/\\_____/\\___/  \\_/ \\____/\\_|  |_/\\___/\\____/ \\____/");
        MetadataHandler.PLUGIN = this;
        Bukkit.getLogger().info("By MagmaGuy - v. " + MetadataHandler.PLUGIN.getDescription().getVersion());

        ServerTime.startTickCounter();

        if (Bukkit.getServer().spigot().getConfig().getDouble("settings.attribute.maxHealth.max") < 100000000) {
            Bukkit.getServer().spigot().getConfig().set("settings.attribute.maxHealth.max", 100000000);
            try {
                File spigotConfigContainer = new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + "/spigot.yml").toString());
                Bukkit.getServer().spigot().getConfig().save(spigotConfigContainer);
                new InfoMessage("New default max health set correctly!");
            } catch (IOException e) {
                new WarningMessage("Failed to save max health value! For the plugin to work correctly, you should increase your max health on the spigot.yml config file to " + 100000000);
            }
        }

        //Remove entities that should not exist
        CrashFix.startupCheck();

        /*
        New config loading
         */
        initializeConfigs();

        if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises"))
            DisguiseEntity.initialize();

        if (worldGuardIsEnabled)
            Bukkit.getLogger().info("[EliteMobs] WorldGuard compatibility is enabled!");
        else
            Bukkit.getLogger().warning("[EliteMobs] WorldGuard compatibility is not enabled!");

        //Enable Vault
        try {
            VaultCompatibility.vaultSetup();
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EliteMobs] Something went wrong with the vault configuration - your Vault " +
                    "version is probably not compatible with this EliteMobs version. Please contact the dev about this error.");
            VaultCompatibility.VAULT_ENABLED = false;
        }

        //Hook up all listeners, some depend on config
        EventsRegistrer.registerEvents();

        //Launch the local data cache
        PlayerData.initializeDatabaseConnection();
        ElitePlayerInventory.initialize();

        //Get world list
        worldScanner();

        //Start the repeating tasks such as scanners
        launchRunnables();

        //launch events
        ActionEvent.initializeBlueprintEvents();
        TimedEvent.initializeBlueprintEvents();

        //launch internal clock for attack cooldown
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.launchInternalClock();

        /*
        Initialize mob values
         */
        PluginMobProperties.initializePluginMobValues();

        /*
        Cache animation vectors
         */
        MinorPowerStanceMath.initializeVectorCache();
        MajorPowerStanceMath.initializeVectorCache();

        /*
        Scan for loaded SuperMobs
         */
        PassiveEliteMobDeathHandler.SuperMobScanner.scanSuperMobs();

        /*
        Check for new plugin version
         */
        VersionChecker.updateComparer();
        if (!VersionChecker.pluginIsUpToDate)
            this.getServer().getPluginManager().registerEvents(new VersionWarner(), this);

        /*
        Launch quests
         */
        //QuestRefresher.generateNewQuestMenus();

        // Small check to make sure that PlaceholderAPI is installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Placeholders placeholders = new Placeholders();
            placeholders.register();
            this.placeholders = placeholders;
        }

        //Enable stats
        metrics = new Metrics(this, 1081);
        //Initialize custom charts
        new CustomCharts();

        //Imports custom configurations and mindungeons from the import folder
        ConfigurationImporter.initializeConfigs();

        //Import custom items after potentially importing new items
        new CustomItemsConfig();
        CustomItem.initializeCustomItems();
        LootTables.initialize();

        //Load minidungeons, most of all load the worlds of minidungeons
        new DungeonPackagerConfig();
        //Load Adventurer's Guild
        if (AdventurersGuildConfig.guildWorldIsEnabled) {
            try {
                CustomWorldLoading.startupWorldInitialization();
                AdventurersGuildCommand.defineTeleportLocation();
                if (AdventurersGuildConfig.guildWorldLocation == null)
                    AdventurersGuildConfig.toggleGuildInstall();
            } catch (Exception e) {
                AdventurersGuildConfig.toggleGuildInstall();
                new WarningMessage("Failed to initialize the Adventurer's Guild Hub! It is now disabled. You can try to" +
                        "reenable it in /em setup");
            }
        }

        //Load all regional bosses
        new CustomBossesConfig();
        new CustomTreasureChestsConfig();


        //Find the stats of bosses in minidungeons
        for (Minidungeon minidungeon : Minidungeon.minidungeons.values()) {
            if (minidungeon.getDungeonPackagerConfigFields().getDungeonLocationType() != null)
                if (minidungeon.getDungeonPackagerConfigFields().getDungeonLocationType().equals(DungeonPackagerConfigFields.DungeonLocationType.WORLD))
                    minidungeon.quantifyWorldBosses();
                else if (minidungeon.getDungeonPackagerConfigFields().getDungeonLocationType().equals(DungeonPackagerConfigFields.DungeonLocationType.SCHEMATIC))
                    minidungeon.quantifySchematicBosses(false);
        }

        //Initialize npcs
        new NPCsConfig();

        //Initialize custom spawn methods, this runs late because it compares loaded worlds against worlds listed in the config
        try {
            new CustomSpawnConfig();
        } catch (Exception ex) {
            new WarningMessage("You are using a version of Spigot or a branch thereof (Paper, Purpur, so on) that is (probably) HORRIBLY outdated!" +
                    " This issue will probably be fixed if you update your server version to the latest patch of the version you are running.");
            new WarningMessage(
                    " This does not mean that you have to update your Minecraft version, but it does mean you must update your server version to the latest patch" +
                            " available for that Minecraft version. Download from trustworthy sources, as if you download Spigot from some random website other than Spigot," +
                            " you are probably not getting the latest version (and also there's a high chance you'll get a virus).");
        }

        new CustomQuestsConfig();

        //Commands
        new CommandHandler();
    }

    @Override
    public void onLoad() {
        //WorldGuard hook
        try {
            worldGuardIsEnabled = WorldGuardCompatibility.initialize();
        } catch (NoClassDefFoundError | IllegalStateException ex) {
            Bukkit.getLogger().warning("[EliteMobs] Error loading WorldGuard. EliteMob-specific flags will not work." +
                    " Except if you just reloaded the plugin, in which case they will totally work.");
            worldGuardIsEnabled = false;
        }
        if (!worldGuardIsEnabled)
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
                worldGuardIsEnabled = true;

    }

    @Override
    public void onDisable() {

        new InfoMessage("Starting EliteMobs shutdown sequence...");

        new InfoMessage("Regenerating exploded blocks...");
        Explosion.regenerateAllPendingBlocks();

        new InfoMessage("Cancelling tasks...");
        Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);

        new InfoMessage("Spinning Regional Bosses down...");
        //save all pending respawns
        RegionalBossEntity.save();
        RegionalBossEntity.getTrackableCustomBosses().clear();
        RegionalBossEntity.getRegionalBossEntitySet().clear();

        new InfoMessage("Wiping Elite entities clean...");
        EntityTracker.wipeShutdown();

        new InfoMessage("Clearing valid worlds...");
        validWorldList.clear();
        new InfoMessage("Clearing zone based worlds...");
        zoneBasedSpawningWorlds.clear();
        new InfoMessage("Clearing config regional elites...");
        CustomBossesConfigFields.regionalElites.clear();
        new InfoMessage("Clearing config natural elites...");
        CustomBossesConfigFields.getNaturallySpawnedElites().clear();
        new InfoMessage("Clearing custom enchantments...");
        CustomEnchantment.getCustomEnchantments().clear();
        new InfoMessage("Clearing custom items...");
        CustomItem.getCustomItems().clear();
        CustomItem.getCustomItemStackList().clear();
        CustomItem.getCustomItemStackShopList().clear();
        CustomItem.getLimitedItem().clear();
        CustomItem.getScalableItems().clear();
        CustomItem.getFixedItems().clear();
        CustomItem.getTieredLoot().clear();
        CustomItem.getWeighedFixedItems().clear();
        new InfoMessage("Clearing Minidungeons...");
        Minidungeon.minidungeons.clear();
        RegionalBossEntity.regionalBossesShutdown();

        new InfoMessage("Unregistering placeholders...");
        if (this.placeholders != null)
            ((Placeholders) placeholders).unregister();

        new InfoMessage("Unregistering handlers...");
        HandlerList.unregisterAll(MetadataHandler.PLUGIN);

        new InfoMessage("Clearing Treasure Chests...");
        TreasureChest.clearTreasureChests();

        new InfoMessage("Untracking quests...");
        QuestTracking.clear();

        //save cached data
        Bukkit.getLogger().info("[EliteMobs] Saving EliteMobs databases...");
        PlayerData.closeConnection();
        Bukkit.getLogger().info("[EliteMobs] All done! Good night.");

    }

    /*
    Repeating tasks that run as long as the server is on
     */
    private void launchRunnables() {
        if (!zoneBasedSpawningWorlds.isEmpty())
            Grid.initializeGrid();
        int eggTimerInterval = 20 * 60 * 10 / DefaultConfig.superMobStackAmount;
        new PlayerPotionEffects();
        if (MobPropertiesConfig.getMobProperties().get(EntityType.CHICKEN).isEnabled() && DefaultConfig.superMobStackAmount > 0) {
            new EggRunnable().runTaskTimer(this, eggTimerInterval, eggTimerInterval);
        }
        //save regional bosses when the files update
        RegionalBossEntity.regionalDataSaver();
    }

}
