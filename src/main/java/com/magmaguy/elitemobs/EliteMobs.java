package com.magmaguy.elitemobs;

/*
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.api.EMDungeonLocator;
import com.magmaguy.elitemobs.api.EliteMobsInitializedEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.KeepNeutralsAngry;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.config.commands.CommandsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfig;
import com.magmaguy.elitemobs.dungeons.DungeonProtector;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.economy.VaultCompatibility;
import com.magmaguy.elitemobs.entitytracker.CustomProjectileData;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.ActionEvent;
import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonKillPercentageObjective;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonKillTargetObjective;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.customenchantments.*;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.items.potioneffects.custom.Harm;
import com.magmaguy.elitemobs.items.potioneffects.custom.Heal;
import com.magmaguy.elitemobs.items.potioneffects.custom.Saturation;
import com.magmaguy.elitemobs.menus.*;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerItem;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerManager;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.*;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.ScriptAction;
import com.magmaguy.elitemobs.powers.scripts.ScriptListener;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import com.magmaguy.elitemobs.powers.specialpowers.TrackingFireballSupport;
import com.magmaguy.elitemobs.powerstances.MajorPowerStanceMath;
import com.magmaguy.elitemobs.powerstances.MinorPowerStanceMath;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.menus.QuestInventoryMenu;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.SkillSystemMigration;
import com.magmaguy.elitemobs.skills.SkillXPBar;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusInitializer;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.thirdparty.bstats.CustomCharts;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.custommodels.modelengine.ModelEngineReservedAddresses;
import com.magmaguy.elitemobs.thirdparty.placeholderapi.Placeholders;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.BossBarUtil;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import com.magmaguy.elitemobs.wormhole.WormholeManager;
import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.initialization.PluginInitializationConfig;
import com.magmaguy.magmacore.initialization.PluginInitializationContext;
import com.magmaguy.magmacore.location.LocationQueryRegistry;
import com.magmaguy.magmacore.util.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EliteMobs extends JavaPlugin {

    public static List<World> validWorldList = new ArrayList<>();
    public static boolean worldGuardIsEnabled = false;
    public static Metrics metrics;
    public Object placeholders = null;

    public static void initializeConfigs() {
        new CustomModelsConfig();
        new DefaultConfig();
        new ItemSettingsConfig();
        new ProceduralItemGenerationSettingsConfig();
        new StaticItemNamesConfig();
        PotionEffectsConfig.initializeConfigs();
        new EconomySettingsConfig();
        new EventsConfig();
        new SkillsConfig();
        new EnchantmentsConfig();
        new AntiExploitConfig();
        new CombatTagConfig();
        new AntiExploitConfig();
        new AdventurersGuildConfig();
        new ValidWorldsConfig();

        new MenusConfig();
        new LuaPowersConfig();
        new PowersConfig();
        MobPropertiesConfig.initializeConfigs();
        CustomEnchantment.initializeCustomEnchantments();

        new MobCombatSettingsConfig();
        CommandsConfig.initializeConfigs();
        new DiscordSRVConfig();
        new PeaceBannerConfig();
        new CustomEventsConfig();
        new QuestsConfig();
        new WormholesConfig();
        new ArenasConfig();
        //ModelsConfig.initializeConfig();
        new DungeonsConfig();
        new CommandMessagesConfig();
        new InitializeConfig();
        new SoundsConfig();
        new com.magmaguy.elitemobs.config.GamblingConfig();
    }

    public static void worldScanner() {
        for (World world : Bukkit.getWorlds())
            if (ValidWorldsConfig.getInstance().getFileConfiguration().getBoolean("validWorlds." + world.getName())) {
                validWorldList.add(world);
            }

    }

    @Override
    public void onEnable() {
        // ═══════════════════════════════════════════════════════
        // SYNC PREAMBLE -- quick setup, stays on main thread
        // ═══════════════════════════════════════════════════════
        MetadataHandler.pluginState = PluginState.INITIALIZING;
        MetadataHandler.shutdownRequested = false;

        Bukkit.getLogger().info(" _____ _     _____ _____ ________  ______________  _____");
        Bukkit.getLogger().info("|  ___| |   |_   _|_   _|  ___|  \\/  |  _  | ___ \\/  ___|");
        Bukkit.getLogger().info("| |__ | |     | |   | | | |__ | .  . | | | | |_/ /\\ `--.");
        Bukkit.getLogger().info("|  __|| |     | |   | | |  __|| |\\/| | | | | ___ \\ `--. \\");
        Bukkit.getLogger().info("| |___| |_____| |_  | | | |___| |  | \\ \\_/ / |_/ //\\__/ /");
        Bukkit.getLogger().info("\\____/\\_____/\\___/  \\_/ \\____/\\_|  |_/\\___/\\____/ \\____/");
        Bukkit.getLogger().info("By MagmaGuy - v. " + MetadataHandler.PLUGIN.getDescription().getVersion());

        MagmaCore.onEnable(this);

        if (VersionChecker.serverVersionOlderThan(21, 0)) {
            Logger.warn("You are running a Minecraft version older than 1.21.0! EliteMobs 9.0 and later are only compatible with Minecraft 1.21.0 or later, if you are running an older Minecraft version you will need to use a pre-9.0 version of EliteMobs.");
            MetadataHandler.pluginState = PluginState.UNINITIALIZED;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        NMSManager.initializeAdapter(this);

        if (Bukkit.getServer().spigot().getConfig().getDouble("settings.attribute.maxHealth.max") < Double.MAX_VALUE) {
            Bukkit.getServer().spigot().getConfig().set("settings.attribute.maxHealth.max", Double.MAX_VALUE);
            try {
                File spigotConfigContainer = new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + "/spigot.yml").toString());
                Bukkit.getServer().spigot().getConfig().save(spigotConfigContainer);
                Logger.info("New default max health set correctly!");
            } catch (IOException e) {
                Logger.warn("Failed to save max health value! For the plugin to work correctly, you should increase your max health on the spigot.yml config file to " + Double.MAX_VALUE);
            }
        }

        //Remove entities that should not exist
        CrashFix.startupCheck();

        MagmaCore.startInitialization(this,
                new PluginInitializationConfig("EliteMobs", "elitemobs.*", 34),
                this::asyncInitialization,
                this::syncInitialization,
                () -> {
                    if (MetadataHandler.shutdownRequested) {
                        Logger.warn("EliteMobs init cancelled -- server shutting down.");
                        return;
                    }
                    MetadataHandler.pluginState = PluginState.INITIALIZED;
                    Bukkit.getPluginManager().callEvent(new EliteMobsInitializedEvent());
                    Logger.info("EliteMobs fully initialized!");
                    if (MetadataHandler.pendingReloadSender != null) {
                        Logger.sendMessage(MetadataHandler.pendingReloadSender, com.magmaguy.elitemobs.config.CommandMessagesConfig.getReloadSuccessMessage());
                        MetadataHandler.pendingReloadSender = null;
                    }
                },
                throwable -> {
                    MetadataHandler.pluginState = PluginState.UNINITIALIZED;
                    MetadataHandler.pendingReloadSender = null;
                    throwable.printStackTrace();
                });
    }

    private void asyncInitialization(PluginInitializationContext initializationContext) {
        initializationContext.step("Configs");
        initializeConfigs();
        initializationContext.step("Database");
        new DatabaseConfig();
        initializationContext.step("Skill Bonuses Config");
        new SkillBonusesConfig();
        initializationContext.step("Events");
        ActionEvent.initializeBlueprintEvents();
        TimedEvent.initializeBlueprintEvents();
        initializationContext.step("Mob Properties");
        PluginMobProperties.initializePluginMobValues();
        initializationContext.step("Power Stances");
        MinorPowerStanceMath.initializeVectorCache();
        MajorPowerStanceMath.initializeVectorCache();
        initializationContext.step("Configuration Export");
        ConfigurationExporter.initializeConfigs();
        //Initialize importer before loading content configs so that newly imported
        //files are on disk when the config classes scan for them.
        initializationContext.step("Content Importer");
        MagmaCore.initializeImporter(this);
        initializationContext.step("Custom Items");
        new CustomItemsConfig();
        CustomItem.initializeCustomItems();
        initializationContext.step("Loot Tables");
        LootTables.initialize();
        initializationContext.step("Content Packages Config");
        new ContentPackagesConfig();
        initializationContext.step("Custom Bosses Config");
        new CustomBossesConfig();
        initializationContext.step("NPCs Config");
        new NPCsConfig();
        initializationContext.step("Wormholes Config");
        new WormholeConfig();
        initializationContext.step("Arenas Config");
        new CustomArenasConfig();
        initializationContext.step("Quests Config");
        new CustomQuestsConfig();
        initializationContext.step("Special Items Config");
        new SpecialItemSystemsConfig();
    }

    private void syncInitialization(PluginInitializationContext initializationContext) {
        //Refresh dungeon versions from disk after importer extracts new content packages.
        //The importer may overwrite YAML files with updated dungeonVersion values, but the
        //in-memory configs were loaded before the importer ran, so they need to be refreshed.
        initializationContext.step("Dungeon Versions");
        ContentPackagesConfig.refreshDungeonVersions();

        //Initializes custom models
        initializationContext.step("Custom Models");
        CustomModel.initialize();
        //Reserves ModelEngine addresses if present
        ModelEngineReservedAddresses.reserve();

        if (worldGuardIsEnabled) Logger.info("WorldGuard compatibility is enabled!");
        else Logger.info("WorldGuard compatibility is not enabled!");

        //Enable Vault
        initializationContext.step("Economy");
        try {
            VaultCompatibility.vaultSetup();
        } catch (Exception e) {
            Logger.warn("Something went wrong with the vault configuration - your Vault " + "version is probably not compatible with this EliteMobs version. Please contact the dev about this error.");
            VaultCompatibility.VAULT_ENABLED = false;
        }

        //Hook up all listeners, some depend on config
        initializationContext.step("Event Listeners");
        EventsRegistrer.registerEvents();

        //Launch the local data cache
        initializationContext.step("Player Database");
        PlayerData.initializeDatabaseConnection();
        ElitePlayerInventory.initialize();

        //Initialize skill system
        initializationContext.step("Skills System");
        SkillSystemMigration.initialize();
        SkillBonusInitializer.initialize();
        // Re-apply skill bonuses for any players already online (e.g. after plugin reload)
        for (Player p : Bukkit.getOnlinePlayers()) SkillBonusRegistry.applyAllBonuses(p);
        CombatLevelDisplay.initialize();

        //Initialize gambling system
        initializationContext.step("Gambling System");
        com.magmaguy.elitemobs.economy.GamblingEconomyHandler.initialize();
        com.magmaguy.elitemobs.gambling.DebtCollectorManager.initialize();
        com.magmaguy.elitemobs.gambling.GamblingDenOwnerDisplay.initialize();

        //Get world list
        initializationContext.step("World Scanner");
        worldScanner();

        //Start the repeating tasks such as scanners
        initializationContext.step("Scheduled Tasks");
        launchRunnables();
        com.magmaguy.elitemobs.utils.MessageThrottler.start();

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

        //Initialize em package content, such as world loading
        initializationContext.step("Content Packages");
        ContentPackagesConfig.initializePackages();
        EMDungeonLocator emDungeonLocator = new EMDungeonLocator();
        LocationQueryRegistry.registerDungeonLocator(emDungeonLocator);

        // Per-dungeon protection check — EliteMobsWorld is only populated for
        // worlds where the content pack opted into protection (isProtect=true
        // in the package config). Sandbox/unprotected EM worlds register as
        // owned but report is_protected=false.
        java.util.function.Predicate<org.bukkit.Location> emProtectionFn = loc ->
                loc != null && loc.getWorld() != null
                        && com.magmaguy.elitemobs.dungeons.EliteMobsWorld.isEliteMobsWorld(loc.getWorld().getUID());

        // Kind-tagging for typed queries — every EM-owned location reports its
        // package type and DungeonSizeCategory so cross-plugin scripts can branch
        // on whether they're in an adventure, minidungeon, raid, etc., without
        // hardcoding world names. Always includes "elitemobs" as a stable base tag.
        java.util.function.Function<org.bukkit.Location, java.util.Set<String>> emKindsFn = loc -> {
            if (loc == null || loc.getWorld() == null) return java.util.Collections.emptySet();
            com.magmaguy.elitemobs.dungeons.EMPackage pkg =
                    com.magmaguy.elitemobs.dungeons.EMPackage.getContent(loc.getWorld().getName());
            if (!(pkg instanceof com.magmaguy.elitemobs.dungeons.WorldPackage)) return java.util.Collections.emptySet();
            java.util.Set<String> kinds = new java.util.HashSet<>();
            kinds.add("elitemobs");
            kinds.add("world_package");
            if (pkg instanceof com.magmaguy.elitemobs.dungeons.WorldDungeonPackage) kinds.add("open_world_dungeon");
            com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields.DungeonSizeCategory size =
                    pkg.getContentPackagesConfigFields().getDungeonSizeCategory();
            if (size != null) kinds.add(size.name().toLowerCase(java.util.Locale.ROOT));
            return kinds;
        };

        // Cross-plugin discovery via Bukkit's ServicesManager. Any consumer plugin
        // shading MagmaCore (FMM, MegaBlockSurvivors, BetterStructures, …) can call
        // em.location.owned_by(loc, "EliteMobs"), .owners(loc), .is_protected(loc),
        // .kinds_at(loc), .has_kind(loc, "adventure") and see this entry, independent
        // of which classloader hosts their Lua scripts.
        com.magmaguy.magmacore.location.api.LocationOwnership.registerTyped(
                this, "EliteMobs", emKindsFn, emProtectionFn);

        // Same protection function on EM's own LocationQueryRegistry copy so
        // EM-internal callers of is_protected get the right answer too.
        LocationQueryRegistry.registerProtectionProvider(new com.magmaguy.magmacore.location.RegionProtectionProvider() {
            @Override public boolean isProtected(org.bukkit.Location loc) { return emProtectionFn.test(loc); }
            @Override public String providerName() { return "EliteMobs"; }
        });

        // Legacy bridge: older FMM jars don't query LocationOwnership yet, so push
        // directly into their isolated registry. Newer FMM picks it up via the
        // ServicesManager path above and ignores this side; harmless either way.
        if (Bukkit.getPluginManager().getPlugin("FreeMinecraftModels") != null) {
            try {
                com.magmaguy.freeminecraftmodels.api.LocationAPI.registerDungeonLocator(
                        "EliteMobs", emDungeonLocator::contains);
            } catch (NoClassDefFoundError e) {
                // FMM too old to expose LocationAPI — older versions also lack
                // the LocationOwnership ServicesManager path, so just warn.
                Logger.warn("Failed to bridge dungeon locator into FreeMinecraftModels — the "
                        + "installed FMM jar is older than 2.4.0 (missing LocationAPI). "
                        + "is_in_dungeon in FMM scripts will not see EliteMobs dungeons.");
            }
        }
        com.magmaguy.elitemobs.scripting.LuaEntityEnricher.register();

        //Initialize custom & regional bosses
        initializationContext.step("Custom Bosses");
        CustomBossesConfig.initializeBosses();

        //Initialize treasure chests (requires block state access)
        initializationContext.step("Treasure Chests");
        new CustomTreasureChestsConfig();

        //Initialize NPCs (requires entity spawning)
        initializationContext.step("NPCs");
        NPCsConfig.initializeNPCs();

        //Initialize em package content
        initializationContext.step("EM Packages");
        for (EMPackage emPackage : EMPackage.getEmPackages().values())
            try {
                if (emPackage.isInstalled()) emPackage.initializeContent();
            } catch (Exception exception) {
                Logger.warn("Failed to load EliteMobs Package " + emPackage.getContentPackagesConfigFields().getFilename() + " !");
                exception.printStackTrace();
            }

        //Re-resolve wormhole destinations now that packages are loaded
        for (EMPackage emPackage : EMPackage.getEmPackages().values())
            if (emPackage.isInstalled())
                for (Wormhole wormhole : Wormhole.getWormholes())
                    wormhole.onDungeonInstall(emPackage.getContentPackagesConfigFields().getFilename());

        //Initialize custom spawn methods, this runs late because it compares loaded worlds against worlds listed in the config
        initializationContext.step("Custom Spawns");
        try {
            new CustomSpawnConfig();
        } catch (Exception ex) {
            Logger.warn("You are using a version of Spigot or a branch thereof (Paper, Purpur, so on) that is (probably) HORRIBLY outdated!" + " This issue will probably be fixed if you update your server version to the latest patch of the version" +
                    " you are running.");
            Logger.warn(" This does not mean that you have to update your Minecraft version, but it does mean you must update your server version to the latest patch" + " available for that Minecraft version. Download from trustworthy sources, as if you download Spigot from some random website other than Spigot," + " you are probably not getting the latest version (and also there's a high chance you'll get a virus).");
        }

        //Commands
        initializationContext.step("Commands");
        CommandHandler.registerCommands();

        // Peace Banner system
        PeaceBannerManager.loadData();
        PeaceBannerItem.registerRecipe();

        /*
        Check for new plugin version or for dungeon updates
         */
        VersionChecker.check();

        DynamicQuest.startRandomizingQuests();
        CustomBossEntity.startUpdatingDynamicLevels();

        EntityTracker.managedEntityWatchdog();
        //initialize the centralized wormhole manager
        WormholeManager.getInstance(false);

        // Regenerate cached item stacks after all plugins have loaded
        // This ensures custom skins are applied correctly (ResourcePackManager may not be loaded during initial item generation)
        initializationContext.step("Item Cache");
        CustomItem.regenerateCachedItemStacks();
    }

    @Override
    public void onLoad() {
        //Initializes some core utilities that are shared across MagmaGuy's plugins
        MetadataHandler.PLUGIN = this;
        MagmaCore.createInstance(this);

        //WorldGuard hook
        try {
            worldGuardIsEnabled = WorldGuardCompatibility.initialize();
        } catch (NoClassDefFoundError | IllegalStateException ex) {
            Logger.warn("Error loading WorldGuard. EliteMob-specific flags will not work." + " Except if you just reloaded the plugin, in which case they will totally work.");
            worldGuardIsEnabled = false;
        }
        if (!worldGuardIsEnabled)
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) worldGuardIsEnabled = true;

    }

    @Override
    public void onDisable() {
        MetadataHandler.shutdownRequested = true;
        MagmaCore.requestInitializationShutdown(this);
        if (MetadataHandler.pluginState == PluginState.INITIALIZING) {
            Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
            MetadataHandler.pluginState = PluginState.UNINITIALIZED;
            MagmaCore.shutdown(this);
            Logger.info("EliteMobs shutdown during init -- cancelled async loading.");
            return;
        }
        if (MetadataHandler.pluginState == PluginState.UNINITIALIZED) {
            MagmaCore.shutdown(this);
            return;
        }
        Logger.info("Starting EliteMobs shutdown sequence...");
        Explosion.shutdown();
        Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
        Wormhole.shutdown();
        RegionalBossEntity.save();
        RegionalBossEntity.getTrackableCustomBosses().clear();
        RegionalBossEntity.getRegionalBossEntitySet().clear();
        InstancedBossEntity.shutdown();
        NPCEntity.shutdown();
        PersistentObjectHandler.shutdown();
        EntityTracker.wipeShutdown();
        TimedEvent.shutdown();
        ActionEvent.shutdown();
        PeaceBannerManager.shutdown();
        PeaceBannerItem.unregisterRecipe();
        validWorldList.clear();
        CustomBossesConfigFields.getRegionalElites().clear();
        CustomEnchantment.getCustomEnchantmentMap().clear();
        CustomItem.getCustomItems().clear();
        CustomItem.getCustomItemStackList().clear();
        CustomItem.getCustomItemStackShopList().clear();
        CustomItem.getLimitedItems().clear();
        CustomItem.getScalableItems().clear();
        CustomItem.getFixedItems().clear();
        CustomItem.getTieredLoot().clear();
        CustomItem.getWeighedFixedItems().clear();
        EMPackage.shutdown();
        RegionalBossEntity.regionalBossesShutdown();
        if (this.placeholders != null) ((Placeholders) placeholders).unregister();
        HandlerList.unregisterAll(MetadataHandler.PLUGIN);
        TreasureChest.shutdown();
        WorldOperationQueue.shutdown();
        MatchInstance.shutdown();
        CustomProjectileData.shutdown();
        DynamicQuest.shutdown();
        ProceduralShopMenu.shutdown();
        EliteMobsWorld.shutdown();
        Navigation.shutdown();
        BossBarUtil.shutdown();
        ScriptAction.shutdown();
        CustomMusic.shutdown();
        CustomBossEntity.shutdown();
        com.magmaguy.elitemobs.combatsystem.displays.BossHealthDisplay.shutdown();
        // Memory leak fixes - clear static collections
        SummonWolfEnchantment.SummonWolfEnchantmentEvent.shutdown();
        SummonMerchantEnchantment.SummonMerchantEvents.shutdown();
        FlamethrowerEnchantment.shutdown();
        LightningEnchantment.LightningEnchantmentEvents.shutdown();
        ItemLootShower.shutdown();
        com.magmaguy.elitemobs.utils.MessageThrottler.shutdown();
        TrackingFireballSupport.shutdown();
        VersionChecker.shutdown();
        KeepNeutralsAngry.shutdown();
        LootMenu.shutdown();
        TeleportsPage.TeleportsPageEvents.shutdown();
        BossTrackingPage.BossTrackingPageEvents.shutdown();
        // Additional memory leak fixes
        PlayerDeathMessageByEliteMob.shutdown();
        NPCInteractions.shutdown();
        com.magmaguy.elitemobs.items.GearRestrictionHandler.shutdown();
        com.magmaguy.elitemobs.antiexploit.FarmingProtection.shutdown();
        NPCProximitySensor.shutdown();
        PlayerQuestCooldowns.shutdown();
        GetLootMenu.shutdown();
        TransitiveBlockCommand.shutdown();
        DrillingEnchantment.shutdown();
        Saturation.shutdown();
        Heal.shutdown();
        Harm.shutdown();
        ScriptListener.shutdown();
        CombatEnterScanPower.shutdown();
        QuestTracking.shutdown();
        SharedLootTable.shutdown();
        // Menu shutdowns
        CustomShopMenu.shutdown();
        SellMenu.shutdown();
        RepairMenu.shutdown();
        ScrapperMenu.shutdown();
        UnbindMenu.shutdown();
        // Gambling menu shutdowns
        com.magmaguy.elitemobs.menus.gambling.BettingMenu.shutdown();
        com.magmaguy.elitemobs.menus.gambling.BlackjackGame.shutdown();
        com.magmaguy.elitemobs.menus.gambling.CoinFlipGame.shutdown();
        com.magmaguy.elitemobs.menus.gambling.HigherLowerGame.shutdown();
        com.magmaguy.elitemobs.menus.gambling.SlotMachineGame.shutdown();
        com.magmaguy.elitemobs.gambling.DebtCollectorManager.shutdown();
        com.magmaguy.elitemobs.gambling.GamblingDenOwnerDisplay.shutdown();
        com.magmaguy.elitemobs.economy.GamblingEconomyHandler.shutdown();
        DynamicDungeonBrowser.shutdown();
        InstancedDungeonBrowser.shutdown();
        PlasmaBootsEnchantment.PlasmaBootsEnchantmentEvents.shutdown();
        EarthquakeEnchantment.EarthquakeEnchantmentEvents.shutdown();
        BuyOrSellMenu.BuyOrSellMenuEvents.shutdown();
        Quest.shutdown();
        QuestInventoryMenu.shutdown();
        StatsPage.StatsPageEvents.shutdown();
        GearPage.GearPageEvents.shutdown();
        CommandsPage.CommandsPageEvents.shutdown();
        CoverPage.CoverPageEvents.shutdown();
        SkillsPage.SkillsPageEvents.shutdown();
        SkillSystemMigration.shutdown();
        SkillBonusInitializer.shutdown();
        SkillXPBar.shutdown();
        CombatLevelDisplay.shutdown();
        ArenaMenu.ArenaMenuEvents.shutdown();
        ItemEnchantmentMenu.ItemEnchantMenuEvents.shutdown();
        EliteScrollMenu.EliteScrollMenuEvents.shutdown();
        // Third pass memory leak fixes
        CrashFix.shutdown();
        com.magmaguy.elitemobs.commands.admin.RemoveCommand.shutdown();
        DungeonProtector.shutdown();
        DungeonKillPercentageObjective.shutdown();
        DungeonKillTargetObjective.shutdown();
        ConfigurationLocation.shutdown();
        // Fourth pass memory leak fixes
        ElitePlayerInventory.shutdown();
        KeepNeutralsAngry.shutdown();
        Navigation.shutdown();
        GameClock.shutdown();
        DynamicQuest.shutdown();
        DungeonInstance.shutdown();
        ArenaInstance.shutdown();
        LuaPowerManager.shutdown();
        // Final pass memory leak fixes
        ElitePower.shutdown();
        EliteScriptBlueprint.shutdown();
        EliteMobProperties.shutdown();
        Logger.info("Saving EliteMobs databases...");
        PlayerData.closeConnection();
        MagmaCore.shutdown(this);
        MetadataHandler.pluginState = PluginState.UNINITIALIZED;
        Logger.info("All done! Good night.");
    }

    /*
    Repeating tasks that run as long as the server is on
     */
    private void launchRunnables() {
        //save regional bosses when the files update
        GameClock.initialize();
        RegionalBossEntity.regionalDataSaver();
    }

}
