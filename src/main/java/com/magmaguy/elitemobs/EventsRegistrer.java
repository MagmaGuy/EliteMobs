package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.collateralminecraftchanges.*;
import com.magmaguy.elitemobs.combatsystem.*;
import com.magmaguy.elitemobs.combatsystem.antiexploit.*;
import com.magmaguy.elitemobs.combatsystem.combattag.CombatTag;
import com.magmaguy.elitemobs.combatsystem.combattag.TeleportTag;
import com.magmaguy.elitemobs.combatsystem.displays.DamageDisplay;
import com.magmaguy.elitemobs.combatsystem.displays.HealthDisplay;
import com.magmaguy.elitemobs.commands.getLootMenu;
import com.magmaguy.elitemobs.commands.shops.BuyOrSellMenu;
import com.magmaguy.elitemobs.commands.shops.CustomShopMenu;
import com.magmaguy.elitemobs.commands.shops.ProceduralShopMenu;
import com.magmaguy.elitemobs.commands.shops.SellMenu;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.actionevents.KrakenEvent;
import com.magmaguy.elitemobs.events.actionevents.MiningEvent;
import com.magmaguy.elitemobs.events.actionevents.TreeChoppingEvent;
import com.magmaguy.elitemobs.events.mobs.Kraken;
import com.magmaguy.elitemobs.events.timedevents.DeadMoonEvent;
import com.magmaguy.elitemobs.events.timedevents.MeteorEvent;
import com.magmaguy.elitemobs.events.timedevents.SmallTreasureGoblinEvent;
import com.magmaguy.elitemobs.gamemodes.nightmaremodeworld.DaylightWatchdog;
import com.magmaguy.elitemobs.gamemodes.zoneworld.ZoneWarner;
import com.magmaguy.elitemobs.initialsetup.PermissionlessModeWarning;
import com.magmaguy.elitemobs.items.*;
import com.magmaguy.elitemobs.items.customenchantments.*;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.elitemobs.mobconstructor.MergeHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.AdvancedAggroManager;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobs.passive.*;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.npcs.NPCChunkLoad;
import com.magmaguy.elitemobs.npcs.NPCDamageEvent;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.playerdata.PlayerStatsTracker;
import com.magmaguy.elitemobs.powers.AggroPrevention;
import com.magmaguy.elitemobs.powers.bosspowers.*;
import com.magmaguy.elitemobs.powers.defensivepowers.InvulnerabilityArrow;
import com.magmaguy.elitemobs.powers.defensivepowers.InvulnerabilityFallDamage;
import com.magmaguy.elitemobs.powers.defensivepowers.InvulnerabilityFire;
import com.magmaguy.elitemobs.powers.defensivepowers.InvulnerabilityKnockback;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonPillar;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonTrackingArrow;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieBloat;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieFriends;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieNecronomicon;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieParents;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.*;
import com.magmaguy.elitemobs.powers.offensivepowers.*;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.VisualEffectObfuscator;
import com.magmaguy.elitemobs.quests.QuestsMenu;
import com.magmaguy.elitemobs.quests.QuestsTracker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardDungeonFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardEliteMobOnlySpawnFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class EventsRegistrer {

    public static void registerEvents() {

        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = MetadataHandler.PLUGIN;

        if (!DefaultConfig.setupDone)
            pluginManager.registerEvents(new PermissionlessModeWarning(), plugin);

        pluginManager.registerEvents(new PlayerData.PlayerDataEvents(), plugin);
        pluginManager.registerEvents(new ElitePlayerInventory.ElitePlayerInventoryEvents(), plugin);
        pluginManager.registerEvents(new PlayerStatsTracker(), plugin);

        //Load passive mobs TODO: find generic alternative to this
        pluginManager.registerEvents(new ChickenHandler(), plugin);
        pluginManager.registerEvents(new CowHandler(), plugin);
        pluginManager.registerEvents(new MushroomCowHandler(), plugin);
        pluginManager.registerEvents(new PassiveEliteMobDeathHandler(), plugin);
        pluginManager.registerEvents(new PigHandler(), plugin);
        pluginManager.registerEvents(new SheepHandler(), plugin);
        pluginManager.registerEvents(new FindSuperMobs(), plugin);

        //Mob damage
        pluginManager.registerEvents(new PlayerDamagedByEliteMobHandler(), plugin);
        pluginManager.registerEvents(new EliteMobDamagedByPlayerHandler(), plugin);
        pluginManager.registerEvents(new EliteCreeperExplosionHandler(), plugin);
        pluginManager.registerEvents(new EliteMobGenericDamagedHandler(), plugin);
        pluginManager.registerEvents(new EliteMobDamagedByEliteMobHandler(), plugin);
        if (MobCombatSettingsConfig.enableDeathMessages)
            pluginManager.registerEvents(new PlayerDeathMessageByEliteMob(), plugin);

        //Mob loot
        pluginManager.registerEvents(new DefaultDropsHandler(), plugin);
        pluginManager.registerEvents(new ItemLootShower.ItemLootShowerEvents(), plugin);

        //npcs
        pluginManager.registerEvents(new NPCChunkLoad(), plugin);

        //potion effects
        pluginManager.registerEvents(new PlayerPotionEffects(), plugin);

        //getloot AdventurersGuildMenu
        pluginManager.registerEvents(new getLootMenu(), plugin);

        /*
        Register API events
         */
        pluginManager.registerEvents(new EliteMobDeathEvent.EliteMobDeathEventFilter(), plugin);
        pluginManager.registerEvents(new EliteMobTargetPlayerEvent.EliteMobTargetPlayerEventFilter(), plugin);
        pluginManager.registerEvents(new EliteMobDamagedEvent.EliteMobDamageEventFilter(), plugin);
        pluginManager.registerEvents(new PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter(), plugin);
        pluginManager.registerEvents(new EliteMobDamagedByEliteMobEvent.EliteMobDamagedByEliteMobFilter(), plugin);
        pluginManager.registerEvents(new EliteMobEnterCombatEvent.EliteMobEnterCombatEventFilter(), plugin);

        /*
        While these powers could be registered in a more automated way, I realized that it's also a bad way of getting
        silent errors without ever noticing, and ultimately the amount of manual input required is pretty minimal
         */
        //Minor mob powers
        pluginManager.registerEvents(new InvulnerabilityArrow(), plugin);
        pluginManager.registerEvents(new InvulnerabilityFallDamage(), plugin);
        pluginManager.registerEvents(new InvulnerabilityFire(), plugin);
        pluginManager.registerEvents(new InvulnerabilityKnockback(), plugin);
        pluginManager.registerEvents(new BonusLoot(), plugin);
        pluginManager.registerEvents(new Taunt(), plugin);
        pluginManager.registerEvents(new Corpse(), plugin);
        pluginManager.registerEvents(new Implosion(), plugin);
        pluginManager.registerEvents(new AttackArrow(), plugin);
        pluginManager.registerEvents(new ArrowFireworks(), plugin);
        pluginManager.registerEvents(new AttackBlinding(), plugin);
        pluginManager.registerEvents(new AttackFire(), plugin);
        pluginManager.registerEvents(new AttackFireball(), plugin);
        pluginManager.registerEvents(new AttackFreeze(), plugin);
        pluginManager.registerEvents(new AttackGravity(), plugin);
        pluginManager.registerEvents(new AttackLightning(), plugin);
        pluginManager.registerEvents(new AttackPoison(), plugin);
        pluginManager.registerEvents(new AttackPush(), plugin);
        pluginManager.registerEvents(new AttackWeakness(), plugin);
        pluginManager.registerEvents(new AttackWeb(), plugin);
        pluginManager.registerEvents(new AttackWither(), plugin);
        pluginManager.registerEvents(new AttackVacuum(), plugin);
        pluginManager.registerEvents(new ArrowRain(), plugin);
        pluginManager.registerEvents(new GroundPound(), plugin);

        //Major mob powers
        pluginManager.registerEvents(new SkeletonPillar(), plugin);
        pluginManager.registerEvents(new SkeletonTrackingArrow(), plugin);
        pluginManager.registerEvents(new ZombieBloat(), plugin);
        pluginManager.registerEvents(new ZombieFriends(), plugin);
        pluginManager.registerEvents(new ZombieNecronomicon(), plugin);
        pluginManager.registerEvents(new ZombieParents(), plugin);

        //boss powers
        pluginManager.registerEvents(new SpiritWalk(), plugin);
        pluginManager.registerEvents(new GoldShotgun(), plugin);
        pluginManager.registerEvents(new GoldExplosion(), plugin);
        pluginManager.registerEvents(new Flamethrower(), plugin);
        if (EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled())
            pluginManager.registerEvents(new SoulbindEnchantment.SoulbindEnchantmentEvents(), plugin);
        pluginManager.registerEvents(new FlamePyre(), plugin);
        pluginManager.registerEvents(new SummonTheReturned(), plugin);
        pluginManager.registerEvents(new HyperLoot(), plugin);
        pluginManager.registerEvents(new SummonRaug(), plugin);
        pluginManager.registerEvents(new SummonTheReturned(), plugin);
        pluginManager.registerEvents(new SummonEmbers(), plugin);
        pluginManager.registerEvents(new MeteorShower(), plugin);
        pluginManager.registerEvents(new BulletHell(), plugin);
        pluginManager.registerEvents(new DeathSlice(), plugin);
        pluginManager.registerEvents(new CustomSummonPower.CustomSummonPowerEvent(), plugin);

        //Custom bosses
        pluginManager.registerEvents(new CustomBossEntity.CustomBossEntityEvents(), plugin);
        pluginManager.registerEvents(new PhaseBossEntity.PhaseBossEntityListener(), plugin);
        pluginManager.registerEvents(new RegionalBossEntity.RegionalBossEntityEvents(), plugin);
        pluginManager.registerEvents(new AdvancedAggroManager(), plugin);

        //Metadata (player purger)
        pluginManager.registerEvents(new MetadataHandler(), plugin);

        //Mob merger
        pluginManager.registerEvents(new MergeHandler(), plugin);

        //Natural EliteMobs Spawning
        if (MobCombatSettingsConfig.doNaturalMobSpawning)
            pluginManager.registerEvents(new EntityTracker(), plugin);
        //Fix lingering entity after crashes
        pluginManager.registerEvents(new CrashFix(), plugin);

        //Natural Mob Metadata Assigner
        pluginManager.registerEvents(new NaturalMobSpawnEventHandler(), plugin);

        //Visual effects
        pluginManager.registerEvents(new EffectEventHandlers(), plugin);
        if (MobCombatSettingsConfig.obfuscateMobPowers)
            pluginManager.registerEvents(new VisualEffectObfuscator(), plugin);

        //Loot
        if (ItemSettingsConfig.doEliteMobsLoot) {
            pluginManager.registerEvents(new LootTables(), plugin);
            pluginManager.registerEvents(new PlaceEventPrevent(), plugin);
        }

        //Shops
        pluginManager.registerEvents(new ProceduralShopMenu(), plugin);
        pluginManager.registerEvents(new CustomShopMenu(), plugin);
        pluginManager.registerEvents(new BuyOrSellMenu(), plugin);
        pluginManager.registerEvents(new SellMenu(), plugin);

        //Minecraft behavior canceller
        pluginManager.registerEvents(new ChunkUnloadMetadataPurge(), plugin);
        pluginManager.registerEvents(new EntityDeathDataFlusher(), plugin);
        if (DefaultConfig.preventCreeperDamageToPassiveMobs)
            pluginManager.registerEvents(new PreventCreeperPassiveEntityDamage(), plugin);

        //Antiexploits
        pluginManager.registerEvents(new PreventMountExploit(), plugin);
        pluginManager.registerEvents(new PreventDarkroomExploit(), plugin);
        pluginManager.registerEvents(new PreventLargeDarkroomExploit(), plugin);
        //TODO: THIS IS BROKEN DUE TO SUMMONING POWERS pluginManager.registerEvents(new PreventDensityExploit(), plugin);
        pluginManager.registerEvents(new PreventTowerExploit(), plugin);
        pluginManager.registerEvents(new PreventEndermanHeightExploit(), plugin);
        if (AntiExploitConfig.noItemPickup)
            pluginManager.registerEvents(new PreventItemPickupByMobs(), plugin);
        if (AntiExploitConfig.ambientDamageExploit)
            pluginManager.registerEvents(new AmbientDamageExploit(), plugin);
        pluginManager.registerEvents(new HoneyBlockJumpExploit(), plugin);
        pluginManager.registerEvents(new EliteMobDamagedByPlayerAntiExploitListener(), plugin);


        //Initialize events
        pluginManager.registerEvents(new EliteEvent.AbstractEliteEventEvents(), plugin);
        pluginManager.registerEvents(new DeadMoonEvent(), plugin);
        pluginManager.registerEvents(new SmallTreasureGoblinEvent(), plugin);
        pluginManager.registerEvents(new MeteorEvent(), plugin);

        if (com.magmaguy.elitemobs.config.events.EventsConfig.getEventFields("kraken.yml").isEnabled()) {
            pluginManager.registerEvents(new Kraken(), plugin);
            pluginManager.registerEvents(new KrakenEvent(), plugin);
        }
        if (com.magmaguy.elitemobs.config.events.EventsConfig.getEventFields("balrog.yml").isEnabled()) {
            pluginManager.registerEvents(new MiningEvent(), plugin);
        }
        if (com.magmaguy.elitemobs.config.events.EventsConfig.getEventFields("fae.yml").isEnabled()) {
            pluginManager.registerEvents(new TreeChoppingEvent(), plugin);
        }


        //Set up health and damage displays
        if (MobCombatSettingsConfig.displayHealthOnHit)
            pluginManager.registerEvents(new HealthDisplay(), plugin);
        if (MobCombatSettingsConfig.displayDamageOnHit)
            pluginManager.registerEvents(new DamageDisplay(), plugin);

        //Initialize items from custom events
        pluginManager.registerEvents(new FlamethrowerEnchantment.FlamethrowerEnchantmentEvents(), plugin);
        pluginManager.registerEvents(new MeteorShowerEnchantment.MeteorShowerEvents(), plugin);
        pluginManager.registerEvents(new DrillingEnchantment(), plugin);
        pluginManager.registerEvents(new IceBreakerEnchantment.IceBreakerEnchantmentEvent(), plugin);

        //Initialize adventurer's guild
        pluginManager.registerEvents(new GuildRankMenuHandler(), plugin);
        //register quests
        pluginManager.registerEvents(new QuestsMenu(), plugin);
        pluginManager.registerEvents(new QuestsTracker(), plugin);

        //Combat tag
        if (CombatTagConfig.enableCombatTag)
            pluginManager.registerEvents(new CombatTag(), plugin);
        if (CombatTagConfig.enableTeleportTimer)
            pluginManager.registerEvents(new TeleportTag(), plugin);


        //Prevent elitemob on elitemob aggro
        pluginManager.registerEvents(new AggroPrevention(), plugin);

        //Player effect when a rare item is on the ground
        if (ItemSettingsConfig.doRareDropsEffect)
            pluginManager.registerEvents(new RareDropEffect(), plugin);

        //NPCs
        pluginManager.registerEvents(new NPCDamageEvent(), plugin);
        pluginManager.registerEvents(new NPCInteractions(), plugin);
        pluginManager.registerEvents(new NPCProximitySensor(), plugin);
        pluginManager.registerEvents(new FindNewWorlds(), plugin);
        pluginManager.registerEvents(new WorldGuardSpawnEventBypasser(), plugin);
        pluginManager.registerEvents(new WorldGuardEliteMobOnlySpawnFlag(), plugin);
        pluginManager.registerEvents(new WorldGuardDungeonFlag(), plugin);

        pluginManager.registerEvents(new EntityTransformPreventer(), plugin);
        pluginManager.registerEvents(new EliteBlazeWaterDamagePrevention(), plugin);
        pluginManager.registerEvents(new PreventEliteEquipmentDrop(), plugin);

        pluginManager.registerEvents(new TreasureChest.TreasureChestEvents(), plugin);

        //Zone based spawning
        pluginManager.registerEvents(new ZoneWarner(), plugin);
        pluginManager.registerEvents(new DaylightWatchdog(), plugin);

        //On death commands
        pluginManager.registerEvents(new OnDeathCommands(), plugin);

        //Player stuff
        pluginManager.registerEvents(new GuildRank.GuildRankEvents(), plugin);

    }

}
