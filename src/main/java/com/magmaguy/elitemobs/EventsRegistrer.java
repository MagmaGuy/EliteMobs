package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.adventurersguild.AdventurersGuildMenu;
import com.magmaguy.elitemobs.adventurersguild.MaxHealthHandler;
import com.magmaguy.elitemobs.adventurersguild.SpawnControl;
import com.magmaguy.elitemobs.antiexploit.*;
import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.collateralminecraftchanges.*;
import com.magmaguy.elitemobs.combattag.CombatTag;
import com.magmaguy.elitemobs.combattag.TeleportTag;
import com.magmaguy.elitemobs.commands.LootGUI;
import com.magmaguy.elitemobs.commands.shops.BuyOrSellMenu;
import com.magmaguy.elitemobs.commands.shops.CustomShopMenu;
import com.magmaguy.elitemobs.commands.shops.ProceduralShopMenu;
import com.magmaguy.elitemobs.commands.shops.SellMenu;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.actionevents.KrakenEvent;
import com.magmaguy.elitemobs.events.actionevents.MiningEvent;
import com.magmaguy.elitemobs.events.actionevents.TreeChoppingEvent;
import com.magmaguy.elitemobs.events.mobs.Kraken;
import com.magmaguy.elitemobs.events.timedevents.DeadMoonEvent;
import com.magmaguy.elitemobs.events.timedevents.SmallTreasureGoblinEvent;
import com.magmaguy.elitemobs.items.*;
import com.magmaguy.elitemobs.items.customenchantments.FlamethrowerEnchantment;
import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import com.magmaguy.elitemobs.mobconstructor.MergeHandler;
import com.magmaguy.elitemobs.mobconstructor.displays.DamageDisplay;
import com.magmaguy.elitemobs.mobconstructor.displays.DisplayMob;
import com.magmaguy.elitemobs.mobconstructor.displays.HealthDisplay;
import com.magmaguy.elitemobs.mobs.passive.*;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.npcs.NPCDamageEvent;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
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
import com.magmaguy.elitemobs.powers.miscellaneouspowers.BonusLoot;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.Corpse;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.Implosion;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.Taunt;
import com.magmaguy.elitemobs.powers.offensivepowers.*;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.VisualEffectObfuscator;
import com.magmaguy.elitemobs.quests.QuestsMenu;
import com.magmaguy.elitemobs.quests.QuestsTracker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class EventsRegistrer {

    public static void registerEvents() {

        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = MetadataHandler.PLUGIN;

        //Load passive mobs TODO: find generic alternative to this
        pluginManager.registerEvents(new ChickenHandler(), plugin);
        pluginManager.registerEvents(new CowHandler(), plugin);
        pluginManager.registerEvents(new MushroomCowHandler(), plugin);
        pluginManager.registerEvents(new PassiveEliteMobDeathHandler(), plugin);
        pluginManager.registerEvents(new PigHandler(), plugin);
        pluginManager.registerEvents(new SheepHandler(), plugin);
        pluginManager.registerEvents(new FindSuperMobs(), plugin);

        //Mob damage
        pluginManager.registerEvents(new CombatSystem(), plugin);
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_DEATH_MESSAGES))
            pluginManager.registerEvents(new PlayerDeathMessageByEliteMob(), plugin);

        //Mob loot
        pluginManager.registerEvents(new DefaultDropsHandler(), plugin);
        pluginManager.registerEvents(new ItemLootShower(), plugin);

        //potion effects
        pluginManager.registerEvents(new PotionEffectApplier(), plugin);

        //getloot AdventurersGuildMenu
        pluginManager.registerEvents(new LootGUI(), plugin);

        /*
        Register API events
         */
        pluginManager.registerEvents(new EliteMobDamagedByPlayerEvent.EntityDamagedByEntityFilter(), plugin);
        pluginManager.registerEvents(new EliteMobDeathEvent.EliteMobDeathEventFilter(), plugin);
        pluginManager.registerEvents(new EliteMobTargetPlayerEvent.EliteMobTargetPlayerEventFilter(), plugin);
        pluginManager.registerEvents(new EliteMobDamageEvent.EliteMobDamageEventFilter(), plugin);
        pluginManager.registerEvents(new PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter(), plugin);

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
        pluginManager.registerEvents(new FlamePyre(), plugin);
        pluginManager.registerEvents(new SummonTheReturned(), plugin);
        pluginManager.registerEvents(new HyperLoot(), plugin);
        pluginManager.registerEvents(new SummonRaug(), plugin);
        pluginManager.registerEvents(new SummonTheReturned(), plugin);

        //Custom bosses
        pluginManager.registerEvents(new CustomBossEntity.CustomBossEntityEvents(), plugin);

        //Metadata (player purger)
        pluginManager.registerEvents(new MetadataHandler(), plugin);

        //Mob merger
        pluginManager.registerEvents(new MergeHandler(), plugin);

        //Natural EliteMobs Spawning
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.NATURAL_MOB_SPAWNING)) {
            pluginManager.registerEvents(new EntityTracker(), plugin);
        }

        //Natural Mob Metadata Assigner
        pluginManager.registerEvents(new NaturalMobSpawnEventHandler(), plugin);

        //Visual effects
        pluginManager.registerEvents(new EffectEventHandlers(), plugin);
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.OBFUSCATE_MOB_POWERS))
            pluginManager.registerEvents(new VisualEffectObfuscator(), plugin);

        //Loot
        if (ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.ENABLE_PLUGIN_LOOT)) {
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
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.CREEPER_PASSIVE_DAMAGE_PREVENTER))
            pluginManager.registerEvents(new PreventCreeperPassiveEntityDamage(), plugin);

        //Antiexploits
        pluginManager.registerEvents(new PreventMountExploit(), plugin);
        pluginManager.registerEvents(new PreventDarkroomExploit(), plugin);
        pluginManager.registerEvents(new PreventLargeDarkroomExploit(), plugin);
        pluginManager.registerEvents(new PreventDensityExploit(), plugin);
        pluginManager.registerEvents(new PreventTowerExploit(), plugin);
        pluginManager.registerEvents(new PreventEndermanHeightExploit(), plugin);


        //Initialize events
        pluginManager.registerEvents(new EliteEvent.AbstractEliteEventEvents(), plugin);
        pluginManager.registerEvents(new DeadMoonEvent(), plugin);
        pluginManager.registerEvents(new SmallTreasureGoblinEvent(), plugin);

        if (ConfigValues.eventsConfig.getBoolean(EventsConfig.KRAKEN_ENABLED)) {
            pluginManager.registerEvents(new Kraken(), plugin);
            pluginManager.registerEvents(new KrakenEvent(), plugin);
        }
        if (ConfigValues.eventsConfig.getBoolean(EventsConfig.BALROG_ENABLED)) {
            pluginManager.registerEvents(new MiningEvent(), plugin);
        }
        if (ConfigValues.eventsConfig.getBoolean(EventsConfig.FAE_ENABLED)) {
            pluginManager.registerEvents(new TreeChoppingEvent(), plugin);
        }


        //Set up health and damage displays
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_HEALTH_ON_HIT))
            pluginManager.registerEvents(new HealthDisplay(), plugin);
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_DAMAGE_ON_HIT))
            pluginManager.registerEvents(new DamageDisplay(), plugin);

        //Initialize items from custom events
        pluginManager.registerEvents(new FlamethrowerEnchantment(), plugin);

        //Initialize adventurer's guild
        if (AdventurersGuildConfig.enableAdventurersGuild)
            pluginManager.registerEvents(new AdventurersGuildMenu(), plugin);
        if (AdventurersGuildConfig.addMaxHealth)
            pluginManager.registerEvents(new MaxHealthHandler(), plugin);
        pluginManager.registerEvents(new SpawnControl(), plugin);
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

        //Refresh display case Elite Mobs
        pluginManager.registerEvents(new DisplayMob(), plugin);

        //Player effect when a rare item is on the ground
        if (ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.ENABLE_RARE_DROP_EFFECT))
            pluginManager.registerEvents(new RareDropEffect(), plugin);

        //NPCs
        pluginManager.registerEvents(new NPCDamageEvent(), plugin);
        pluginManager.registerEvents(new NPCInteractions(), plugin);
        pluginManager.registerEvents(new NPCProximitySensor(), plugin);
        pluginManager.registerEvents(new FindNewWorlds(), plugin);

    }

}
