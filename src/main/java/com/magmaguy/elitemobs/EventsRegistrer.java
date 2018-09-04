package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.adventurersguild.AdventurersGuildGUI;
import com.magmaguy.elitemobs.collateralminecraftchanges.*;
import com.magmaguy.elitemobs.combattag.CombatTag;
import com.magmaguy.elitemobs.commands.LootGUI;
import com.magmaguy.elitemobs.commands.guiconfig.GUIConfigHandler;
import com.magmaguy.elitemobs.commands.guiconfig.configurers.MobSpawningAndLoot;
import com.magmaguy.elitemobs.commands.guiconfig.configurers.ValidMobsConfigurer;
import com.magmaguy.elitemobs.commands.shops.CustomShopHandler;
import com.magmaguy.elitemobs.commands.shops.ItemSaleEvent;
import com.magmaguy.elitemobs.commands.shops.ShopHandler;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.events.SmallTreasureGoblin;
import com.magmaguy.elitemobs.events.actionevents.BalrogEvent;
import com.magmaguy.elitemobs.events.actionevents.FaeEvent;
import com.magmaguy.elitemobs.events.actionevents.KrakenEvent;
import com.magmaguy.elitemobs.events.eventitems.ZombieKingAxe;
import com.magmaguy.elitemobs.events.mobs.*;
import com.magmaguy.elitemobs.events.mobs.sharedeventpowers.SpiritWalk;
import com.magmaguy.elitemobs.items.ItemDropper;
import com.magmaguy.elitemobs.items.PlaceEventPrevent;
import com.magmaguy.elitemobs.items.PotionEffectApplier;
import com.magmaguy.elitemobs.mobcustomizer.DamageAdjuster;
import com.magmaguy.elitemobs.mobcustomizer.DefaultDropsHandler;
import com.magmaguy.elitemobs.mobcustomizer.displays.DamageDisplay;
import com.magmaguy.elitemobs.mobcustomizer.displays.DisplayMob;
import com.magmaguy.elitemobs.mobcustomizer.displays.HealthDisplay;
import com.magmaguy.elitemobs.mobmerger.MergeHandler;
import com.magmaguy.elitemobs.mobpowers.AggroPrevention;
import com.magmaguy.elitemobs.mobs.passive.*;
import com.magmaguy.elitemobs.mobscanner.MobScanner;
import com.magmaguy.elitemobs.mobspawning.NaturalMobMetadataAssigner;
import com.magmaguy.elitemobs.mobspawning.NaturalSpawning;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class EventsRegistrer {

    public static void registerEvents() {

        //Load passive mobs TODO: find generic alternative to this
        Bukkit.getServer().getPluginManager().registerEvents(new ChickenHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new CowHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new MushroomCowHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new PassiveEliteMobDeathHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new PigHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new SheepHandler(), MetadataHandler.PLUGIN);

        //Mob damage
        Bukkit.getServer().getPluginManager().registerEvents(new DamageAdjuster(), MetadataHandler.PLUGIN);
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_DEATH_MESSAGES))
            Bukkit.getServer().getPluginManager().registerEvents(new PlayerDeathMessageByEliteMob(), MetadataHandler.PLUGIN);

        //Mob loot
        Bukkit.getServer().getPluginManager().registerEvents(new DefaultDropsHandler(), MetadataHandler.PLUGIN);

        //potion effects
        Bukkit.getServer().getPluginManager().registerEvents(new PotionEffectApplier(), MetadataHandler.PLUGIN);

        //getloot AdventurersGuildGUI
        Bukkit.getServer().getPluginManager().registerEvents(new LootGUI(), MetadataHandler.PLUGIN);

        //config AdventurersGuildGUI
        Bukkit.getServer().getPluginManager().registerEvents(new GUIConfigHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new ValidMobsConfigurer(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new MobSpawningAndLoot(), MetadataHandler.PLUGIN);

        //Minor mob powers
        for (String string : MetadataHandler.defensivePowerList) {

            //don't load powers that require no event listeners
            if (!(string.equalsIgnoreCase("MovementSpeed"))
                    && !(string.equalsIgnoreCase("Invisibility"))
                    && !(string.equalsIgnoreCase("DoubleHealth"))
                    && !(string.equalsIgnoreCase("DoubleDamage"))) {

                try {

                    String earlypath = "com.magmaguy.elitemobs.mobpowers.defensivepowers.";
                    String finalString = earlypath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    Bukkit.getServer().getPluginManager().registerEvents((Listener) instance, MetadataHandler.PLUGIN);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

            }

        }

        for (String string : MetadataHandler.offensivePowerList) {

            //don't load powers that require no event listeners
            if (!(string.equalsIgnoreCase("MovementSpeed"))
                    && !(string.equalsIgnoreCase("Invisibility"))
                    && !(string.equalsIgnoreCase("DoubleHealth"))
                    && !(string.equalsIgnoreCase("DoubleDamage"))
                    && !(string.equalsIgnoreCase("AttackArrow"))
                    && !(string.equalsIgnoreCase("AttackFireball"))) {

                try {

                    String earlypath = "com.magmaguy.elitemobs.mobpowers.offensivepowers.";
                    String finalString = earlypath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    Bukkit.getServer().getPluginManager().registerEvents((Listener) instance, MetadataHandler.PLUGIN);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

            }

        }

        for (String string : MetadataHandler.miscellaneousPowerList) {

            //don't load powers that require no event listeners
            if (!(string.equalsIgnoreCase("MovementSpeed"))
                    && !(string.equalsIgnoreCase("Invisibility"))
                    && !(string.equalsIgnoreCase("DoubleHealth"))
                    && !(string.equalsIgnoreCase("DoubleDamage")))
                try {

                    String earlypath = "com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.";
                    String finalString = earlypath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    Bukkit.getServer().getPluginManager().registerEvents((Listener) instance, MetadataHandler.PLUGIN);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }


        }

        for (String string : MetadataHandler.majorPowerList) {

            //don't load powers that require no event listeners
            if (!(string.equalsIgnoreCase("SkeletonTrackingArrow")))
                try {

                    String earlypath = "com.magmaguy.elitemobs.mobpowers.majorpowers.";
                    String finalString = earlypath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    Bukkit.getServer().getPluginManager().registerEvents((Listener) instance, MetadataHandler.PLUGIN);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

        }

        //Metadata (player purger)
        Bukkit.getServer().getPluginManager().registerEvents(new MetadataHandler(), MetadataHandler.PLUGIN);

        //Mob scanner
        Bukkit.getServer().getPluginManager().registerEvents(new MobScanner(), MetadataHandler.PLUGIN);

        //Mob merger TODO: maybe add a config scan here?
        Bukkit.getServer().getPluginManager().registerEvents(new MergeHandler(), MetadataHandler.PLUGIN);

        //Natural EliteMobs Spawning
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.NATURAL_MOB_SPAWNING))
            Bukkit.getServer().getPluginManager().registerEvents(new NaturalSpawning(), MetadataHandler.PLUGIN);

        //Natural Mob Metadata Assigner
        Bukkit.getServer().getPluginManager().registerEvents(new NaturalMobMetadataAssigner(), MetadataHandler.PLUGIN);

        //Visual effects
        Bukkit.getServer().getPluginManager().registerEvents(new EffectEventHandlers(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new MinorPowerPowerStance(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new MajorPowerPowerStance(), MetadataHandler.PLUGIN);

        //Loot
        if (ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.ENABLE_PLUGIN_LOOT)) {
            Bukkit.getServer().getPluginManager().registerEvents(new ItemDropper(), MetadataHandler.PLUGIN);
            Bukkit.getServer().getPluginManager().registerEvents(new PlaceEventPrevent(), MetadataHandler.PLUGIN);
        }

        //Shops
        Bukkit.getServer().getPluginManager().registerEvents(new ShopHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new CustomShopHandler(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new ItemSaleEvent(), MetadataHandler.PLUGIN);

        //Minecraft behavior canceller

        Bukkit.getServer().getPluginManager().registerEvents(new ChunkUnloadMetadataPurge(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityDeathMetadataFlusher(), MetadataHandler.PLUGIN);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_ITEM_PICKUP))
            Bukkit.getServer().getPluginManager().registerEvents(new PreventMobItemPickup(), MetadataHandler.PLUGIN);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.CREEPER_PASSIVE_DAMAGE_PREVENTER))
            Bukkit.getServer().getPluginManager().registerEvents(new PreventCreeperPassiveEntityDamage(), MetadataHandler.PLUGIN);
        //Prevent exploits
        //Prevent mount exploit
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_MOUNT_EXPLOIT))
            Bukkit.getServer().getPluginManager().registerEvents(new PreventMountExploit(), MetadataHandler.PLUGIN);
        //Prevent darkroom exploit
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_DARKROOM_EXPLOIT))
            Bukkit.getServer().getPluginManager().registerEvents(new PreventDarkroomExploit(), MetadataHandler.PLUGIN);
        //Prevent tower exploit
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_TOWER_EXPLOIT))
            Bukkit.getServer().getPluginManager().registerEvents(new PreventTowerExploit(), MetadataHandler.PLUGIN);

        //Initialize custom events
        Bukkit.getServer().getPluginManager().registerEvents(new SmallTreasureGoblin(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new TreasureGoblin(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new DeadMoon(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new TheReturned(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new ZombieKing(), MetadataHandler.PLUGIN);
        Bukkit.getServer().getPluginManager().registerEvents(new SpiritWalk(), MetadataHandler.PLUGIN);
        if (ConfigValues.eventsConfig.getBoolean(EventsConfig.KRAKEN_ENABLED)) {
            Bukkit.getServer().getPluginManager().registerEvents(new Kraken(), MetadataHandler.PLUGIN);
            Bukkit.getServer().getPluginManager().registerEvents(new KrakenEvent(), MetadataHandler.PLUGIN);
        }
        if (ConfigValues.eventsConfig.getBoolean(EventsConfig.BALROG_ENABLED)) {
            Bukkit.getServer().getPluginManager().registerEvents(new Balrog(), MetadataHandler.PLUGIN);
            Bukkit.getServer().getPluginManager().registerEvents(new BalrogEvent(), MetadataHandler.PLUGIN);
        }
        if (ConfigValues.eventsConfig.getBoolean(EventsConfig.FAE_ENABLED)) {
            Bukkit.getServer().getPluginManager().registerEvents(new FaeEvent(), MetadataHandler.PLUGIN);
            Bukkit.getServer().getPluginManager().registerEvents(new Fae(), MetadataHandler.PLUGIN);
        }

        //Set up health and damage displays
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_HEALTH_ON_HIT))
            Bukkit.getServer().getPluginManager().registerEvents(new HealthDisplay(), MetadataHandler.PLUGIN);
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_DAMAGE_ON_HIT))
            Bukkit.getServer().getPluginManager().registerEvents(new DamageDisplay(), MetadataHandler.PLUGIN);

        //Initialize items from custom events
        Bukkit.getServer().getPluginManager().registerEvents(new ZombieKingAxe(), MetadataHandler.PLUGIN);

        //Initialize adventurer's guild
        Bukkit.getServer().getPluginManager().registerEvents(new AdventurersGuildGUI(), MetadataHandler.PLUGIN);

        //Combat tag
        if (ConfigValues.combatTagConfig.getBoolean(CombatTagConfig.ENABLE_COMBAT_TAG))
            Bukkit.getServer().getPluginManager().registerEvents(new CombatTag(), MetadataHandler.PLUGIN);

        //Prevent elitemob on elitemob aggro
        Bukkit.getServer().getPluginManager().registerEvents(new AggroPrevention(), MetadataHandler.PLUGIN);

        //Refresh display case Elite Mobs
        Bukkit.getServer().getPluginManager().registerEvents(new DisplayMob(), MetadataHandler.PLUGIN);

    }

}
