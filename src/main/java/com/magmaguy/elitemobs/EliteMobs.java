package com.magmaguy.elitemobs;

/*
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.customloot.CustomLootConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.economy.VaultCompatibility;
import com.magmaguy.elitemobs.events.EventLauncher;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobscanner.SuperMobScanner;
import com.magmaguy.elitemobs.npcs.NPCInitializer;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.powerstances.MajorPowerStanceMath;
import com.magmaguy.elitemobs.powerstances.MinorPowerStanceMath;
import com.magmaguy.elitemobs.quests.QuestRefresher;
import com.magmaguy.elitemobs.runnables.EggRunnable;
import com.magmaguy.elitemobs.runnables.EntityScanner;
import com.magmaguy.elitemobs.runnables.PotionEffectApplier;
import com.magmaguy.elitemobs.runnables.ScoreboardUpdater;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.elitemobs.versionnotifier.VersionWarner;
import com.magmaguy.elitemobs.worlds.CustomWorldLoading;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class EliteMobs extends JavaPlugin {

    public static List<World> validWorldList = new ArrayList();
    public static boolean WORLDGUARD_IS_ENABLED = false;

    @Override
    public void onEnable() {

        //Enable stats
        Metrics metrics = new Metrics(this);

        //Initialize custom enchantments
        CustomEnchantmentCache.initialize();

        //Load loot from config
        ConfigValues.initializeConfigurations();
        ConfigValues.initializeCachedConfigurations();


        /*
        New config loading
         */
        CustomEnchantmentsConfig.initializeConfig();
        AntiExploitConfig.initializeConfig();
        CombatTagConfig.initializeConfig();
        GuildRankData.initializeConfig();
        PlayerMoneyData.initializeConfig();
        CustomBossesConfig.initializeConfigs();
        CustomLootConfig.initializeConfigs();
        CustomItem.initializeCustomItems();
        AntiExploitConfig.initializeConfig();
        AdventurersGuildConfig.initializeConfig();
        ValidWorldsConfig.initializeConfig();
        ValidMobsConfig.initializeConfig();
        NPCsConfig.initializeConfigs();
        MenusConfig.initializeConfigs();

        if (WORLDGUARD_IS_ENABLED)
            Bukkit.getLogger().warning("[EliteMobs] WorldGuard compatibility is enabled!");
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
        PlayerData.initializePlayerData();
        PlayerData.synchronizeDatabases();

        //Get world list
        worldScanner();

        //Start the repeating tasks such as scanners
        launchRunnables();

        //Commands
        this.getCommand("elitemobs").setExecutor(new CommandHandler());

        //launch events
        EventLauncher eventLauncher = new EventLauncher();
        eventLauncher.eventRepeatingTask();

        //launch internal clock for attack cooldown
        CombatSystem.launchInternalClock();

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
        SuperMobScanner.scanSuperMobs();

        /*
        Initialize NPCs
         */
        new NPCInitializer();

        /*
        Make sure entities are getting culled - necessary due to some plugins on some servers
         */
        EntityTracker.entityValidator();

        /*
        Check for new plugin version
         */
        VersionChecker.updateComparer();
        if (!VersionChecker.pluginIsUpToDate)
            this.getServer().getPluginManager().registerEvents(new VersionWarner(), this);

        /*
        Initialize anticheat block values
         */
        NonSolidBlockTypes.initializeNonSolidBlocks();

        /*
        Launch quests
         */
        QuestRefresher.generateNewQuestMenus();

        /*
        Load plugin worlds
         */
        CustomWorldLoading.startupWorldInitialization();

    }

    @Override
    public void onLoad() {
        //WorldGuard hook
        try {
            WORLDGUARD_IS_ENABLED = WorldGuardCompatibility.initialize();
        } catch (NoClassDefFoundError | IllegalStateException ex) {
            Bukkit.getLogger().warning("[EliteMobs] Error loading WorldGuard. EliteMob-specific flags will not work." +
                    " Except if you just reloaded the plugin, in which case they will totally work.");
            WORLDGUARD_IS_ENABLED = false;
        }

    }

    @Override
    public void onDisable() {

        Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);

        EntityTracker.shutdownPurger();

        validWorldList.clear();

        //save cached data
        Bukkit.getScheduler().cancelTask(PlayerData.databaseSyncTaskID);
        Bukkit.getLogger().info("[EliteMobs] Saving EliteMobs databases...");
        PlayerData.saveDatabases();
        Bukkit.getLogger().info("[EliteMobs] All saved! Good night.");
        PlayerData.clearPlayerData();

    }

    public static void worldScanner() {
        for (World world : Bukkit.getWorlds())
            if (ValidWorldsConfig.getBoolean("Valid worlds." + world.getName()))
                validWorldList.add(world);
    }

    /*
    Repeating tasks that run as long as the server is on
     */
    public void launchRunnables() {
        int eggTimerInterval = 20 * 60 * 10 / ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT);

        new EntityScanner().runTaskTimer(this, 20, 20 * 10);
        new PotionEffectApplier().runTaskTimer(this, 20, 20 * 5);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ENABLE_POWER_SCOREBOARDS))
            new ScoreboardUpdater().runTaskTimer(this, 20, 20);
        if (ValidMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS) &&
                ValidMobsConfig.getBoolean(ValidMobsConfig.CHICKEN) &&
                ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT) > 0)
            new EggRunnable().runTaskTimer(this, eggTimerInterval, eggTimerInterval);
    }

}
