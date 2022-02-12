package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.collateralminecraftchanges.*;
import com.magmaguy.elitemobs.combatsystem.EliteCreeperExplosionHandler;
import com.magmaguy.elitemobs.combatsystem.EliteMobDamagedByEliteMobHandler;
import com.magmaguy.elitemobs.combatsystem.EliteMobGenericDamagedHandler;
import com.magmaguy.elitemobs.combatsystem.PlayerDamagedByEliteMobHandler;
import com.magmaguy.elitemobs.combatsystem.antiexploit.*;
import com.magmaguy.elitemobs.combatsystem.combattag.CombatTag;
import com.magmaguy.elitemobs.combatsystem.displays.HealthDisplay;
import com.magmaguy.elitemobs.combatsystem.displays.PopupDisplay;
import com.magmaguy.elitemobs.commands.admin.RemoveCommand;
import com.magmaguy.elitemobs.commands.setup.SetupMenu;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.ActionEvent;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.gamemodes.nightmaremodeworld.DaylightWatchdog;
import com.magmaguy.elitemobs.gamemodes.zoneworld.ZoneWarner;
import com.magmaguy.elitemobs.initialsetup.FirstTimeSetup;
import com.magmaguy.elitemobs.items.*;
import com.magmaguy.elitemobs.items.customenchantments.*;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.elitemobs.menus.*;
import com.magmaguy.elitemobs.mobconstructor.MergeHandler;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.*;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBossBlock;
import com.magmaguy.elitemobs.mobs.passive.*;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.npcs.NPCDamageEvent;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.PlayerStatsTracker;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.powers.bosspowers.*;
import com.magmaguy.elitemobs.powers.defensivepowers.InvulnerabilityArrow;
import com.magmaguy.elitemobs.powers.defensivepowers.InvulnerabilityFallDamage;
import com.magmaguy.elitemobs.powers.defensivepowers.InvulnerabilityKnockback;
import com.magmaguy.elitemobs.powers.defensivepowers.ShieldWall;
import com.magmaguy.elitemobs.powers.majorpowers.blaze.TrackingFireball;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.EnderDragonEmpoweredLightning;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonPillar;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonTrackingArrow;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieBloat;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieFriends;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieNecronomicon;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieParents;
import com.magmaguy.elitemobs.powers.meta.Bombardment;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.*;
import com.magmaguy.elitemobs.powers.offensivepowers.*;
import com.magmaguy.elitemobs.powers.specialpowers.EnderCrystalLightningRod;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.VisualEffectObfuscator;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldownsLogout;
import com.magmaguy.elitemobs.thirdparty.modelengine.CustomModel;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardDungeonFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardEliteMobOnlySpawnFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import wormhole.WormholeEntry;

public class EventsRegistrer {

    private static PluginManager pluginManager;
    private static Plugin plugin;

    private EventsRegistrer() {
    }

    public static void registerEvents() {

        pluginManager = Bukkit.getPluginManager();
        plugin = MetadataHandler.PLUGIN;

        register(new FirstTimeSetup());

        register(new com.magmaguy.elitemobs.versionnotifier.VersionChecker.VersionCheckerEvents());

        register(new PlayerData.PlayerDataEvents());
        register(new ElitePlayerInventory.ElitePlayerInventoryEvents());
        register(new PlayerStatsTracker());
        register(new PlayerQuestCooldownsLogout());

        register(new ChickenHandler());
        register(new CowHandler());
        register(new MushroomCowHandler());
        register(new PassiveEliteMobDeathHandler());
        register(new PigHandler());
        register(new SheepHandler());
        register(new FindSuperMobs());
        if (ItemSettingsConfig.isPreventEliteItemEnchantment())
            register(new ItemEnchantmentPrevention());
        if (!VersionChecker.serverVersionOlderThan(15, 2))
            if (ItemSettingsConfig.isPreventEliteItemDiamondToNetheriteUpgrade())
                register(new PreventUpgradeDiamondToNetherite());

        //Mob damage
        register(new PlayerDamagedByEliteMobHandler());
        register(new EliteCreeperExplosionHandler());
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

        //potion effects
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
        register(new SuperMobDamageEvent.SuperMobDamageEventFilter());
        register(new EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter());
        register(new EliteExplosionEvent.EliteExplosionEvents());

        //Wormholes
        register(new WormholeEntry.WormholeEntryEvent());


        /*
        While these powers could be registered in a more automated way, I realized that it's also a bad way of getting
        silent errors without ever noticing, and ultimately the amount of manual input required is pretty minimal
         */
        //Minor mob powers
        register(new InvulnerabilityArrow());
        register(new InvulnerabilityFallDamage());
        register(new InvulnerabilityKnockback());
        register(new BonusLoot());
        register(new Taunt());
        register(new Corpse());
        register(new Implosion());
        register(new AttackArrow());
        register(new ArrowFireworks());
        register(new AttackBlinding());
        register(new AttackFire());
        register(new AttackFireball());
        register(new TrackingFireball.TrackingFireballEvents());
        register(new AttackFreeze());
        register(new AttackGravity());
        register(new AttackLightning());
        register(new AttackPoison());
        register(new AttackPush());
        register(new AttackWeakness());
        register(new AttackWeb());
        register(new AttackWither());
        register(new AttackVacuum());
        register(new ArrowRain());
        register(new GroundPound());
        register(new LightningBolts());
        register(new FrostCone());
        register(new Thunderstorm());
        register(new Firestorm());
        register(new Bombardment.BombardmentEvents());
        register(new ShieldWall.ShieldWallEvents());

        //Major mob powers
        register(new SkeletonPillar());
        register(new SkeletonTrackingArrow());
        register(new ZombieBloat());
        register(new ZombieFriends());
        register(new ZombieNecronomicon());
        register(new ZombieParents());

        //boss powers
        register(new SpiritWalk());
        register(new GoldShotgun());
        register(new GoldExplosion());
        register(new Flamethrower());
        register(new PlasmaBootsEnchantment.PlasmaBootsEnchantmentEvents());
        if (EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled())
            register(new SoulbindEnchantment.SoulbindEnchantmentEvents());
        register(new FlamePyre());
        register(new SummonTheReturned());
        register(new HyperLoot());
        register(new SummonTheReturned());
        register(new SummonEmbers());
        register(new MeteorShower());
        register(new BulletHell());
        register(new DeathSlice());
        register(new FireworksBarrage.FireworksBarrageEvents());
        register(new CustomSummonPower.CustomSummonPowerEvent());
        register(new EnderDragonEmpoweredLightning.EnderDragonEmpoweredLightningEvents());
        register(new CombatEnterScanPower.MajorCombatEnterScanningPowerEvents());
        register(new LightningEnchantment.LightningEnchantmentEvents());
        register(new BonusCoins.BonusCoinsEvents());

        //special powers
        register(new EnderCrystalLightningRod.EnderCrystalLightningRodEvents());

        //Custom bosses
        register(new CustomBossEntity.CustomBossEntityEvents());
        register(new CustomBossDeath());
        register(new SimplePersistentEntity.PersistentEntityEvent());
        register(new CustomBossTaunts());
        register(new PhaseBossEntity.PhaseBossEntityListener());
        register(new RegionalBossEntity.RegionalBossEntityEvents());
        register(new AdvancedAggroManager());
        register(new TransitiveBossBlock());
        register(new TransitiveBlockCommand.TemporaryBossBlockCommandEvents());
        register(new CustomModel.ModelEntityEvents());

        //Metadata (player purger)
        register(new MetadataHandler());

        //Mob merger
        register(new MergeHandler());

        //Natural EliteMobs Spawning
        register(new EntityTracker());
        //Fix lingering entity after crashes
        register(new CrashFix());

        //Natural Mob Metadata Assigner
        register(new NaturalMobSpawnEventHandler());

        //Visual effects
        register(new EffectEventHandlers());
        if (MobCombatSettingsConfig.isObfuscateMobPowers())
            register(new VisualEffectObfuscator());

        //Loot
        if (ItemSettingsConfig.isDoEliteMobsLoot()) {
            register(new LootTables());
            register(new PlaceEventPrevent());
        }

        //Shops
        register(new ProceduralShopMenu());
        register(new CustomShopMenu());
        register(new BuyOrSellMenu());
        register(new SellMenu());
        register(new SetupMenu.SetupMenuListeners());
        register(new ScrapperMenu.ScrapperMenuEvents());
        register(new SmeltMenu.SmeltMenuEvents());
        register(new RepairMenu.RepairMenuEvents());
        register(new RefinerMenu.RefinerMenuEvents());
        register(new EnhancementMenu.EnhancementMenuEvents());
        register(new UnbindMenu.UnbinderMenuEvents());

        //Minecraft behavior canceller
        if (DefaultConfig.isPreventCreeperDamageToPassiveMobs())
            register(new PreventCreeperPassiveEntityDamage());
        if (!VersionChecker.serverVersionOlderThan(15, 0))
            register(new PreventEliteBeeHiveEnter());
        register(new EnderDragonUnstuck());
        if (DefaultConfig.isPreventVanillaReinforcementsForEliteEntities())
            register(new VanillaReinforcementsCanceller());
        register(new LightningSpawnBypass());
        if (ItemSettingsConfig.isEliteDurability())
            register(new AlternativeDurabilityLoss());
        register(new EnderCrystalDamageProtectionBypass());


        //Antiexploits
        register(new PreventMountExploit());
        register(new PreventDarkroomExploit());
        register(new PreventLargeDarkroomExploit());
        register(new PreventTowerExploit());
        register(new PreventEndermanHeightExploit());
        if (AntiExploitConfig.isNoItemPickup())
            register(new PreventItemPickupByMobs());
        if (AntiExploitConfig.isAmbientDamageExploit())
            register(new AmbientDamageExploit());
        if (!VersionChecker.serverVersionOlderThan(14, 0)) {
            register(new HoneyBlockJumpExploit());
        }
        register(new EliteMobDamagedByPlayerAntiExploitListener());

        register(new ActionEvent.ActionEventEvents());

        //Set up health and damage displays
        if (MobCombatSettingsConfig.isDisplayHealthOnHit())
            register(new HealthDisplay());
        if (MobCombatSettingsConfig.isDisplayDamageOnHit())
            register(new PopupDisplay());

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

        //Initialize adventurer's guild
        register(new GuildRankMenuHandler());
        //register quests
        register(new KillObjective.KillObjectiveEvents());
        register(new CustomFetchObjective.CustomFetchObjectiveEvents());
        register(new DialogObjective.DialogObjectiveEvents());
        register(new QuestAcceptEvent.QuestAcceptEventHandler());
        register(new QuestCompleteEvent.QuestCompleteEventHandler());
        register(new QuestLeaveEvent.QuestLeaveEventHandler());
        register(new QuestProgressionEvent.QuestProgressionEventHandler());
        register(new QuestTracking.QuestTrackingEvents());
        register(new CustomQuest.CustomQuestEvents());

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
        register(new FindNewWorlds());
        register(new WorldGuardSpawnEventBypasser());
        register(new WorldGuardEliteMobOnlySpawnFlag());
        register(new WorldGuardDungeonFlag());

        register(new EntityTransformHandler());
        register(new EliteBlazeWaterDamagePrevention());
        register(new PreventEliteEquipmentDrop());
        register(new CombustionPrevention());

        register(new TreasureChest.TreasureChestEvents());

        //Zone based spawning
        register(new ZoneWarner());
        register(new DaylightWatchdog());

        //On death commands
        register(new OnDeathCommands());

        //Player stuff
        register(new GuildRank.GuildRankEvents());

        //Commands
        register(new RemoveCommand.RemoveCommandEvents());

    }

    private static void register(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }

}
