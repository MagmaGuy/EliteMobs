package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.collateralminecraftchanges.*;
import com.magmaguy.elitemobs.combatsystem.EliteMobDamagedByEliteMobHandler;
import com.magmaguy.elitemobs.combatsystem.EliteMobGenericDamagedHandler;
import com.magmaguy.elitemobs.combatsystem.antiexploit.*;
import com.magmaguy.elitemobs.combatsystem.combattag.CombatTag;
import com.magmaguy.elitemobs.combatsystem.displays.BossHealthDisplay;
import com.magmaguy.elitemobs.commands.admin.RemoveCommand;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.dungeons.DungeonBossLockoutHandler;
import com.magmaguy.elitemobs.dungeons.DungeonProtector;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.ActionEvent;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.initialsetup.FirstTimeSetup;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonKillTargetObjective;
import com.magmaguy.elitemobs.items.*;
import com.magmaguy.elitemobs.items.customenchantments.*;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.elitemobs.menus.*;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.*;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBossBlock;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.npcs.NPCDamageEvent;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerListener;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.PlayerStatsTracker;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.*;
import com.magmaguy.elitemobs.powers.BonusCoins;
import com.magmaguy.elitemobs.powers.lua.LuaPowerEvents;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.powers.scripts.ScriptListener;
import com.magmaguy.elitemobs.powers.specialpowers.EnderCrystalLightningRod;
import com.magmaguy.elitemobs.powers.specialpowers.TrackingFireballSupport;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.VisualEffectObfuscator;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.QuestLockoutHandler;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.menus.QuestInventoryMenu;
import com.magmaguy.elitemobs.quests.objectives.ArenaObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldownsLogout;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.SkillSystemMigration;
import com.magmaguy.elitemobs.skills.SkillXPBar;
import com.magmaguy.elitemobs.skills.SkillXPHandler;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusEventHandler;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardDungeonFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardEliteMobOnlySpawnFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardExplosionBlockDamageFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class EventsRegistrer {

    private static PluginManager pluginManager;
    private static Plugin plugin;

    private EventsRegistrer() {
    }

    public static void registerEvents() {

        pluginManager = Bukkit.getPluginManager();
        plugin = MetadataHandler.PLUGIN;

        register(new FirstTimeSetup());

        register(new Navigation());

        register(new DungeonKillTargetObjective.DungeonKillTargetObjectiveListener());

        register(new VersionChecker.VersionCheckerEvents());

        register(new PlayerData.PlayerDataEvents());
        register(new ElitePlayerInventory.ElitePlayerInventoryEvents());
        register(new PlayerStatsTracker());
        register(new SkillXPHandler());
        register(new SkillXPBar());
        register(new SkillSystemMigration.MigrationEvents());
        register(new CombatLevelDisplay());
        register(new SkillBonusEventHandler());
        register(new SkillBonusMenu.SkillBonusMenuEvents());
        register(new PlayerQuestCooldownsLogout());

        if (ItemSettingsConfig.isPreventEliteItemEnchantment())
            register(new ItemEnchantmentPrevention());
        if (ItemSettingsConfig.isPreventEliteItemDisenchantment())
            register(new ItemDisenchantPrevention());

        if (ItemSettingsConfig.isPreventEliteItemDiamondToNetheriteUpgrade())
            register(new PreventUpgradeDiamondToNetherite());

        if (ItemSettingsConfig.isUseEliteItemScrolls())
            register(new EliteScrollMenu.EliteScrollMenuEvents());

        register(new FixPlayerOnLoginOrRespawn());
        register(new EnvironmentalDungeonDamage());
        register(new PlayerQuitCleanup());
        register(new com.magmaguy.elitemobs.wormhole.WormholePlayerListener());

        //Mob damage
        register(new EliteMobGenericDamagedHandler());
        register(new EliteMobDamagedByEliteMobHandler());
        if (MobCombatSettingsConfig.isEnableDeathMessages())
            register(new PlayerDeathMessageByEliteMob());

        //explosion regenerator
        if (DefaultConfig.isDoExplosionRegen())
            register(new Explosion.ExplosionEvent());

        //Mob loot
        register(new DefaultDropsHandler());
        register(new ItemLootShower.ItemLootShowerEvents());

        //potion effects - also initializes the task
        register(new PlayerPotionEffects());

        //getloot AdventurersGuildMenu
        register(new GetLootMenu.GetLootMenuListener());

        /*
        Register API events
         */
        register(new EliteMobDeathEvent.EliteMobDeathEventFilter());
        register(new EliteMobTargetPlayerEvent.EliteMobTargetPlayerEventFilter());
        register(new EliteMobDamagedEvent.EliteMobDamageEventFilter());
        register(new PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter());
        register(new EliteMobDamagedByEliteMobEvent.EliteMobDamagedByEliteMobFilter());
        register(new EliteMobEnterCombatEvent.EliteMobEnterCombatEventFilter());
        register(new PlayerPreTeleportEvent.PlayerPreTeleportEventEvents());
        register(new PlayerTeleportEvent.PlayerTeleportEventExecutor());
        register(new EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter());
        register(new EliteExplosionEvent.EliteExplosionEvents());

        /*
        While these powers could be registered in a more automated way, I realized that it's also a bad way of getting
        silent errors without ever noticing, and ultimately the amount of manual input required is pretty minimal
         */
        //Minor mob powers
        register(new ScriptListener());
        register(new LuaPowerEvents());
        registerPower(new TrackingFireballSupport.Events(), "tracking_fireball.yml");

        //boss powers
        register(new PlasmaBootsEnchantment.PlasmaBootsEnchantmentEvents());
        if (EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled())
            register(new SoulbindEnchantment.SoulbindEnchantmentEvents());
        register(new CustomSummonPower.CustomSummonPowerEvent());
        register(new CombatEnterScanPower.MajorCombatEnterScanningPowerEvents());
        register(new LightningEnchantment.LightningEnchantmentEvents());
        registerPower(new BonusCoins.BonusCoinsEvents(), "bonus_coins.yml");

        //special powers
        register(new EnderCrystalLightningRod.EnderCrystalLightningRodEvents());

        register(new EliteScript.EliteScriptEvents());

        //Custom bosses
        register(new CustomBossEntity.CustomBossEntityEvents());
        register(new FrozenCustomBossKnockbackHandler());
        register(new CustomBossDeath());
        register(new PersistentObjectHandler.PersistentObjectHandlerEvents());
        register(new CustomBossTaunts());
        register(new PhaseBossEntity.PhaseBossEntityListener());
        register(new AdvancedAggroManager());
        register(new TransitiveBossBlock());
        register(new TransitiveBlockCommand.TemporaryBossBlockCommandEvents());
        register(new CustomModel.ModelEntityEvents());

        //Metadata (player purger)
        register(new MetadataHandler());

        //Natural EliteMobs Spawning
        register(new EntityTracker());
        //Fix lingering entity after crashes
        register(new CrashFix());

        //Natural Mob Metadata Assigner
        register(new NaturalMobSpawnEventHandler());

        //Peace Banner zone protection
        if (PeaceBannerConfig.isEnabled())
            register(new PeaceBannerListener());

        //Visual effects
        register(new EffectEventHandlers());
        if (MobCombatSettingsConfig.isObfuscateMobPowers())
            register(new VisualEffectObfuscator());

        //Loot
        if (ItemSettingsConfig.isDoEliteMobsLoot()) {
            register(new LootTables());
            register(new PlaceEventPrevent());
        }

        //player status menu
        register(new CoverPage.CoverPageEvents());
        register(new StatsPage.StatsPageEvents());
        register(new SkillsPage.SkillsPageEvents());
        register(new GearPage.GearPageEvents());
        register(new TeleportsPage.TeleportsPageEvents());
        register(new CommandsPage.CommandsPageEvents());
        register(new BossTrackingPage.BossTrackingPageEvents());
        register(new InstancedDungeonBrowser.InstancedDungeonBrowserEvents());
        register(new DynamicDungeonBrowser.DynamicDungeonBrowserEvents());

        //Shops
        register(new ProceduralShopMenu.ProceduralShopMenuEvents());
        register(new CustomShopMenu.CustomShopMenuEvents());
        register(new BuyOrSellMenu.BuyOrSellMenuEvents());
        register(new SellMenu());
//        register(new SetupMenu.SetupMenuListeners());
        register(new ScrapperMenu.ScrapperMenuEvents());
        register(new RepairMenu.RepairMenuEvents());
        register(new UnbindMenu.UnbinderMenuEvents());
        register(new ItemEnchantmentMenu.ItemEnchantMenuEvents());
        register(new ArrowShopMenu.ArrowShopMenuEvents());

        //Gambling menus
        register(new com.magmaguy.elitemobs.menus.gambling.BettingMenu.BettingMenuEvents());
        register(new com.magmaguy.elitemobs.menus.gambling.BlackjackGame.BlackjackMenuEvents());
        register(new com.magmaguy.elitemobs.menus.gambling.CoinFlipGame.CoinFlipMenuEvents());
        register(new com.magmaguy.elitemobs.menus.gambling.HigherLowerGame.HigherLowerMenuEvents());
        register(new com.magmaguy.elitemobs.menus.gambling.SlotMachineGame.SlotMachineMenuEvents());
        register(new com.magmaguy.elitemobs.gambling.DebtCollectorManager());

        //loot menu
        register(new LootMenu.LootMenuEvents());

        //Minecraft behavior canceller
        register(new PreventEliteBeeHiveEnter());
        register(new EnderDragonUnstuck());
        if (DefaultConfig.isPreventVanillaReinforcementsForEliteEntities())
            register(new VanillaReinforcementsCanceller());
        register(new LightningSpawnBypass());
        if (ItemSettingsConfig.isEliteDurability())
            register(new AlternativeDurabilityLoss());
        register(new EnderCrystalDamageProtectionBypass());
        register(new NPCsBecomeWitches());
        register(new PreventEliteSilverfishBlockEnter());


        //Antiexploits
        register(new PreventMountExploit());
        register(new PreventDarkroomExploit());
        register(new PreventLargeDarkroomExploit());
        register(new PreventEndermanHeightExploit());
        if (AntiExploitConfig.isNoItemPickup())
            register(new PreventItemPickupByMobs());
        if (AntiExploitConfig.isAmbientDamageExploit())
            register(new AmbientDamageExploit());
        register(new HoneyBlockJumpExploit());
        register(new EliteMobDamagedByPlayerAntiExploitListener());
        if (AntiExploitConfig.isNoPathExploit())
            register(new PreventPathfindingExploit());

        register(new ActionEvent.ActionEventEvents());

        //Set up enhanced boss health display system (visual bars, numeric display, boss bars, damage/heal popups)
        BossHealthDisplay bossHealthDisplay = new BossHealthDisplay();
        register(bossHealthDisplay);
        BossHealthDisplay.startMasterUpdateTask();

        //Initialize items from custom events
        register(new FlamethrowerEnchantment.FlamethrowerEnchantmentEvents());
        register(new SummonMerchantEnchantment.SummonMerchantEvents());
        register(new SummonWolfEnchantment.SummonWolfEnchantmentEvent());
        register(new MeteorShowerEnchantment.MeteorShowerEvents());
        register(new DrillingEnchantment.DrillingEnchantmentEvents());
        register(new IceBreakerEnchantment.IceBreakerEnchantmentEvent());
        register(new GrapplingHookEnchantment.GrapplingHookEnchantmentEvents());
        register(new EarthquakeEnchantment.EarthquakeEnchantmentEvents());
        //register(new UnbindEnchantment.UnbindEvents());

        //register quests
        register(new KillObjective.KillObjectiveEvents());
        register(new CustomFetchObjective.CustomFetchObjectiveEvents());
        register(new DialogObjective.DialogObjectiveEvents());
        register(new ArenaObjective.ArenaObjectiveEvents());
        register(new QuestAcceptEvent.QuestAcceptEventHandler());
        register(new QuestCompleteEvent.QuestCompleteEventHandler());
        register(new QuestLeaveEvent.QuestLeaveEventHandler());
        register(new QuestProgressionEvent.QuestProgressionEventHandler());
        register(new QuestTracking.QuestTrackingEvents());
        register(new CustomQuest.CustomQuestEvents());
        register(new QuestInventoryMenu.QuestInventoryMenuEvents());
        register(new ArenaCompleteEvent.ArenaCompleteEventHandler());

        //Songs
        register(new CustomMusic.CustomMusicEvents());

        //Arenas
        register(new ArenaMenu.ArenaMenuEvents());
        register(new ArenaInstance.ArenaInstanceEvents());
        register(new MatchInstance.MatchInstanceEvents());

        //Combat tag
        if (CombatTagConfig.isEnableCombatTag())
            register(new CombatTag());


        //Prevent elitemob on elitemob aggro
        register(new EnderDragonUnstuck.AggroPrevention());

        //Player effect when a rare item is on the ground
        register(new RareDropEffect());

        //NPCs
        register(new NPCDamageEvent());
        register(new NPCInteractions());
        register(new NPCProximitySensor());
        register(new NPCEntity.NPCEntityEvents());
        register(new FindNewWorlds());
        register(new WorldGuardSpawnEventBypasser());
        if (EliteMobs.worldGuardIsEnabled) {
            register(new WorldGuardEliteMobOnlySpawnFlag());
            register(new WorldGuardDungeonFlag());
            register(new WorldGuardExplosionBlockDamageFlag());
        }
        register(new DungeonProtector());
        register(new DungeonBossLockoutHandler());
        register(new QuestLockoutHandler());

        register(new EntityTransformHandler());
        register(new EliteBlazeWaterDamagePrevention());
        register(new PreventEliteEquipmentDrop());
        register(new CombustionPrevention());
        register(new EliteSlimeDeathSplit());
        register(new LightningImmunity());

        register(new TreasureChest.TreasureChestEvents());

        //On death commands
        register(new OnDeathCommands());

        //Player stuff (guild rank events removed - skill system replacement)

        //Commands
        register(new RemoveCommand.RemoveCommandEvents());

    }

    private static void registerPower(Listener listener, String config) {
        if (PowersConfig.getPower(config).isEnabled())
            register(listener);
    }

    private static void register(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }

}
