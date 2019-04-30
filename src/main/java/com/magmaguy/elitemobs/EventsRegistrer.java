package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.adventurersguild.AdventurersGuildMenu;
import com.magmaguy.elitemobs.adventurersguild.MaxHealthBoost;
import com.magmaguy.elitemobs.adventurersguild.SpawnControl;
import com.magmaguy.elitemobs.collateralminecraftchanges.*;
import com.magmaguy.elitemobs.combattag.CombatTag;
import com.magmaguy.elitemobs.combattag.TeleportTag;
import com.magmaguy.elitemobs.commands.LootGUI;
import com.magmaguy.elitemobs.commands.shops.BuyOrSellMenu;
import com.magmaguy.elitemobs.commands.shops.CustomShopHandler;
import com.magmaguy.elitemobs.commands.shops.SellMenu;
import com.magmaguy.elitemobs.commands.shops.ShopHandler;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.events.SmallTreasureGoblin;
import com.magmaguy.elitemobs.events.actionevents.BalrogEvent;
import com.magmaguy.elitemobs.events.actionevents.FaeEvent;
import com.magmaguy.elitemobs.events.actionevents.KrakenEvent;
import com.magmaguy.elitemobs.events.mobs.*;
import com.magmaguy.elitemobs.items.*;
import com.magmaguy.elitemobs.items.customenchantments.FlamethrowerEnchantment;
import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import com.magmaguy.elitemobs.mobconstructor.MergeHandler;
import com.magmaguy.elitemobs.mobconstructor.displays.DamageDisplay;
import com.magmaguy.elitemobs.mobconstructor.displays.DisplayMob;
import com.magmaguy.elitemobs.mobconstructor.displays.HealthDisplay;
import com.magmaguy.elitemobs.mobpowers.AggroPrevention;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.InvulnerabilityArrow;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.InvulnerabilityFallDamage;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.InvulnerabilityFire;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.InvulnerabilityKnockback;
import com.magmaguy.elitemobs.mobpowers.majorpowers.*;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.BonusLoot;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.Corpse;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.Implosion;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.Taunt;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.*;
import com.magmaguy.elitemobs.mobs.passive.*;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.npcs.NPCDamageEvent;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.VisualEffectObfuscator;
import com.magmaguy.elitemobs.quests.QuestsMenu;
import com.magmaguy.elitemobs.utils.VersionChecker;
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
        pluginManager.registerEvents(new ZombieTeamRocket(), plugin);

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
        pluginManager.registerEvents(new ShopHandler(), plugin);
        pluginManager.registerEvents(new CustomShopHandler(), plugin);
        pluginManager.registerEvents(new BuyOrSellMenu(), plugin);
        pluginManager.registerEvents(new SellMenu(), plugin);

        //Minecraft behavior canceller
        pluginManager.registerEvents(new ChunkUnloadMetadataPurge(), plugin);
        pluginManager.registerEvents(new EntityDeathDataFlusher(), plugin);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.CREEPER_PASSIVE_DAMAGE_PREVENTER))
            pluginManager.registerEvents(new PreventCreeperPassiveEntityDamage(), plugin);

        //Prevent exploits
        //Prevent mount exploit
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_MOUNT_EXPLOIT))
            pluginManager.registerEvents(new PreventMountExploit(), plugin);
        //Prevent darkroom exploit
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_DARKROOM_EXPLOIT))
            pluginManager.registerEvents(new PreventDarkroomExploit(), plugin);
        //Prevent large darkroom exploit
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_LARGE_DARKROOM_EXPLOIT))
            pluginManager.registerEvents(new PreventLargeDarkroomExploit(), plugin);
        //Prevent other exploits
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_OTHER_EXPLOITS))
            pluginManager.registerEvents(new PreventOtherExploits(), plugin);
        //Prevent tower exploit
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_TOWER_EXPLOIT))
            pluginManager.registerEvents(new PreventTowerExploit(), plugin);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_ENDERMAN_HEIGHT_EXPLOIT))
            pluginManager.registerEvents(new PreventEndermanHeightExploit(), plugin);


        if (!VersionChecker.currentVersionIsUnder(11, 0)) {
            //Initialize custom events
            pluginManager.registerEvents(new SmallTreasureGoblin(), plugin);
            pluginManager.registerEvents(new TreasureGoblin(), plugin);
            pluginManager.registerEvents(new DeadMoon(), plugin);
            pluginManager.registerEvents(new TheReturned(), plugin);
            pluginManager.registerEvents(new ZombieKing(), plugin);
            pluginManager.registerEvents(new com.magmaguy.elitemobs.mobpowers.bosspowers.SpiritWalk(), plugin);

            if (ConfigValues.eventsConfig.getBoolean(EventsConfig.KRAKEN_ENABLED)) {
                pluginManager.registerEvents(new Kraken(), plugin);
                pluginManager.registerEvents(new KrakenEvent(), plugin);
            }
            if (ConfigValues.eventsConfig.getBoolean(EventsConfig.BALROG_ENABLED)) {
                pluginManager.registerEvents(new Balrog(), plugin);
                pluginManager.registerEvents(new BalrogEvent(), plugin);
            }
            if (ConfigValues.eventsConfig.getBoolean(EventsConfig.FAE_ENABLED)) {
                pluginManager.registerEvents(new FaeEvent(), plugin);
                pluginManager.registerEvents(new Fae(), plugin);
            }
        }


        //Set up health and damage displays
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_HEALTH_ON_HIT))
            pluginManager.registerEvents(new HealthDisplay(), plugin);
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_DAMAGE_ON_HIT))
            pluginManager.registerEvents(new DamageDisplay(), plugin);

        //Initialize items from custom events
        pluginManager.registerEvents(new FlamethrowerEnchantment(), plugin);

        //Initialize adventurer's guild
        if (ConfigValues.adventurersGuildConfig.getBoolean(AdventurersGuildConfig.ENABLE_ADVENTURERS_GUILD))
            pluginManager.registerEvents(new AdventurersGuildMenu(), plugin);
        if (ConfigValues.adventurersGuildConfig.getBoolean(AdventurersGuildConfig.ADD_MAX_HEALTH))
            pluginManager.registerEvents(new MaxHealthBoost(), plugin);
        pluginManager.registerEvents(new SpawnControl(), plugin);
        //register quests
        pluginManager.registerEvents(new QuestsMenu(), plugin);

        //Combat tag
        if (ConfigValues.combatTagConfig.getBoolean(CombatTagConfig.ENABLE_COMBAT_TAG))
            pluginManager.registerEvents(new CombatTag(), plugin);
        if (ConfigValues.combatTagConfig.getBoolean(CombatTagConfig.ENABLE_TELEPORT_TIMER))
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
