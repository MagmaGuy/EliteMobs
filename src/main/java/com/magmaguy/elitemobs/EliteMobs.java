/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs;

/**
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.elitemobs.collateralminecraftchanges.*;
import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.commands.LootGUI;
import com.magmaguy.elitemobs.commands.guiconfig.GUIConfigHandler;
import com.magmaguy.elitemobs.commands.guiconfig.configurers.MobSpawningAndLoot;
import com.magmaguy.elitemobs.commands.guiconfig.configurers.ValidMobsConfigurer;
import com.magmaguy.elitemobs.commands.shops.CustomShopHandler;
import com.magmaguy.elitemobs.commands.shops.ItemSaleEvent;
import com.magmaguy.elitemobs.commands.shops.ShopHandler;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.elitedrops.*;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.events.EventLauncher;
import com.magmaguy.elitemobs.events.SmallTreasureGoblin;
import com.magmaguy.elitemobs.events.eventitems.ZombieKingAxe;
import com.magmaguy.elitemobs.events.mobs.TheReturned;
import com.magmaguy.elitemobs.events.mobs.TreasureGoblin;
import com.magmaguy.elitemobs.events.mobs.ZombieKing;
import com.magmaguy.elitemobs.events.mobs.sharedeventpowers.SpiritWalk;
import com.magmaguy.elitemobs.mobcustomizer.DamageAdjuster;
import com.magmaguy.elitemobs.mobcustomizer.DefaultDropsHandler;
import com.magmaguy.elitemobs.mobcustomizer.displays.DamageDisplay;
import com.magmaguy.elitemobs.mobcustomizer.displays.HealthDisplay;
import com.magmaguy.elitemobs.mobmerger.MergeHandler;
import com.magmaguy.elitemobs.mobs.passive.*;
import com.magmaguy.elitemobs.mobscanner.MobScanner;
import com.magmaguy.elitemobs.naturalmobspawner.NaturalMobMetadataAssigner;
import com.magmaguy.elitemobs.naturalmobspawner.NaturalMobSpawner;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.scoreboard.ScoreboardHandler;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class EliteMobs extends JavaPlugin implements Listener {

    public static List<World> worldList = new ArrayList();

    @Override
    public void onEnable() {

        getLogger().info("EliteMobs - Enabled!");

        //Enable stats
        Metrics metrics = new Metrics(this);

        //Load loot from config
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.loadConfiguration();

        MobPowersConfig mobPowersConfig = new MobPowersConfig();
        mobPowersConfig.initializeConfig();

        ItemsCustomLootListConfig itemsCustomLootListConfig = new ItemsCustomLootListConfig();
        itemsCustomLootListConfig.intializeConfig();

        ItemsCustomLootSettingsConfig itemsCustomLootSettingsConfig = new ItemsCustomLootSettingsConfig();
        itemsCustomLootSettingsConfig.initializeConfig();

        TranslationConfig translationConfig = new TranslationConfig();
        translationConfig.initializeConfig();

        ItemsProceduralSettingsConfig itemsProceduralSettingsConfig = new ItemsProceduralSettingsConfig();
        itemsProceduralSettingsConfig.intializeConfig();

        EconomySettingsConfig economySettingsConfig = new EconomySettingsConfig();
        economySettingsConfig.initializeConfig();

        PlayerMoneyDataConfig playerMoneyDataConfig = new PlayerMoneyDataConfig();
        playerMoneyDataConfig.intializeConfig();

        PlayerCacheConfig playerCacheConfig = new PlayerCacheConfig();
        playerCacheConfig.initializeConfig();

        EventsConfig eventsConfig = new EventsConfig();
        eventsConfig.initializeConfig();

        ValidWorldsConfig validWorldsConfig = new ValidWorldsConfig();
        validWorldsConfig.initializeConfig();

        ValidMobsConfig validMobsConfig = new ValidMobsConfig();
        validMobsConfig.initializeConfig();

        ItemsUniqueConfig itemsUniqueConfig = new ItemsUniqueConfig();
        itemsUniqueConfig.initializeConfig();

        MobCombatSettingsConfig mobCombatSettingsConfig = new MobCombatSettingsConfig();
        mobCombatSettingsConfig.initializeConfig();

        ItemsDropSettingsConfig itemsDropSettingsConfig = new ItemsDropSettingsConfig();
        itemsDropSettingsConfig.initializeConfig();

        ConfigValues.initializeConfigValues();

        //Parse loot
        CustomItemConstructor superDrops = new CustomItemConstructor();
        superDrops.superDropParser();

        UniqueItemConstructor uniqueItemConstructor = new UniqueItemConstructor();
        uniqueItemConstructor.intializeUniqueItems();

        //Get world list
        worldScanner();

        //Load passive mobs TODO: find generic alternative to this
        this.getServer().getPluginManager().registerEvents(new ChickenHandler(), this);
        this.getServer().getPluginManager().registerEvents(new CowHandler(), this);
        this.getServer().getPluginManager().registerEvents(new MushroomCowHandler(), this);
        this.getServer().getPluginManager().registerEvents(new PassiveEliteMobDeathHandler(), this);
        this.getServer().getPluginManager().registerEvents(new PigHandler(), this);
        this.getServer().getPluginManager().registerEvents(new SheepHandler(), this);

        //Start the repeating tasks such as scanners
        repeatingTaskRunner();

        //Hook up all listeners, some depend on config
        this.getServer().getPluginManager().registerEvents(this, this);

        //Mob damage
        this.getServer().getPluginManager().registerEvents(new DamageAdjuster(), this);

        //Mob loot
        this.getServer().getPluginManager().registerEvents(new DefaultDropsHandler(), this);

        //potion effects
        this.getServer().getPluginManager().registerEvents(new PotionEffectApplier(), this);

        //getloot GUI
        this.getServer().getPluginManager().registerEvents(new LootGUI(), this);

        //config GUI
        this.getServer().getPluginManager().registerEvents(new GUIConfigHandler(), this);
        this.getServer().getPluginManager().registerEvents(new ValidMobsConfigurer(), this);
        this.getServer().getPluginManager().registerEvents(new MobSpawningAndLoot(), this);

        //Minor mob powers
        for (String string : MetadataHandler.minorPowerList) {

            //don't load powers that require no event listeners
            if (!(string.equalsIgnoreCase("MovementSpeed"))
                    && !(string.equalsIgnoreCase("Invisibility"))
                    && !(string.equalsIgnoreCase("DoubleHealth"))
                    && !(string.equalsIgnoreCase("DoubleDamage"))) {

                try {

                    String earlypath = "com.magmaguy.elitemobs.mobpowers.minorpowers.";
                    String finalString = earlypath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    this.getServer().getPluginManager().registerEvents((Listener) instance, this);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

            }

        }

        for (String string : MetadataHandler.majorPowerList) {

            //don't load powers that require no event listeners
            try {

                String earlypath = "com.magmaguy.elitemobs.mobpowers.majorpowers.";
                String finalString = earlypath + string;

                Class<?> clazz = Class.forName(finalString);

                Object instance = clazz.newInstance();

                this.getServer().getPluginManager().registerEvents((Listener) instance, this);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }

        //Metadata (player purger)
        this.getServer().getPluginManager().registerEvents(new MetadataHandler(), this);

        //Player cacher
        this.getServer().getPluginManager().registerEvents(new OfflinePlayerCacher(), this);

        //Mob scanner
        this.getServer().getPluginManager().registerEvents(new MobScanner(), this);

        //Mob merger TODO: maybe add a config scan here?
        this.getServer().getPluginManager().registerEvents(new MergeHandler(), this);

        //Natural EliteMobs Spawning
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.NATURAL_MOB_SPAWNING)) {

            this.getServer().getPluginManager().registerEvents(new NaturalMobSpawner(), this);

            getLogger().info("EliteMobs - Natural EliteMob Spawning enabled!");

        }

        //Natural Mob Metadata Assigner
        this.getServer().getPluginManager().registerEvents(new NaturalMobMetadataAssigner(), this);

        //Visual effects
        this.getServer().getPluginManager().registerEvents(new EffectEventHandlers(), this);
        this.getServer().getPluginManager().registerEvents(new MinorPowerPowerStance(), this);
        this.getServer().getPluginManager().registerEvents(new MajorPowerPowerStance(), this);

        //Loot
        this.getServer().getPluginManager().registerEvents(new EliteDropsDropper(), this);
        this.getServer().getPluginManager().registerEvents(new PlaceEventPrevent(), this);


        //Shops
        this.getServer().getPluginManager().registerEvents(new ShopHandler(), this);
        this.getServer().getPluginManager().registerEvents(new CustomShopHandler(), this);
        this.getServer().getPluginManager().registerEvents(new ItemSaleEvent(), this);

        //Minecraft behavior canceller
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.CREEPER_PASSIVE_DAMAGE_PREVENTER)) {

            this.getServer().getPluginManager().registerEvents(new PreventCreeperPassiveEntityDamage(), this);
            getLogger().info("EliteMobs - Creeper passive mob collateral damage canceller enabled!");

        }

        this.getServer().getPluginManager().registerEvents(new ChunkUnloadMetadataPurge(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDeathMetadataFlusher(), this);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_ITEM_PICKUP)) {

            this.getServer().getPluginManager().registerEvents(new PreventMobItemPickup(), this);

        }

        //Initialize custom events
        this.getServer().getPluginManager().registerEvents(new SmallTreasureGoblin(), this);
        this.getServer().getPluginManager().registerEvents(new TreasureGoblin(), this);
        this.getServer().getPluginManager().registerEvents(new DeadMoon(), this);
        this.getServer().getPluginManager().registerEvents(new TheReturned(), this);
        this.getServer().getPluginManager().registerEvents(new ZombieKing(), this);
        this.getServer().getPluginManager().registerEvents(new SpiritWalk(), this);

        //Set up health and damage displays
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_HEALTH_ON_HIT))
            this.getServer().getPluginManager().registerEvents(new HealthDisplay(), this);
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_DAMAGE_ON_HIT))
            this.getServer().getPluginManager().registerEvents(new DamageDisplay(), this);

        //Initialize items from custom events
        this.getServer().getPluginManager().registerEvents(new ZombieKingAxe(), this);

        //Commands
        this.getCommand("elitemobs").setExecutor(new CommandHandler());

        //launch events
        EventLauncher eventLauncher = new EventLauncher();
        eventLauncher.eventRepeatingTask();

    }

    @Override
    public void onDisable() {

        MetadataHandler metadataHandler = new MetadataHandler();

        for (World world : worldList) {

            for (Entity entity : world.getEntities()) {

                metadataHandler.fullMetadataFlush(entity);

                if (entity instanceof Player) {

                    ((Player) entity).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

                }

            }

        }

        CustomItemConstructor.customItemList.clear();
        CustomItemConstructor.staticCustomItemHashMap.clear();
        CustomItemConstructor.dynamicRankedItemStacks.clear();
        UniqueItemConstructor.uniqueItems.clear();

        getLogger().info("EliteMobs - Disabled!");

    }

    public void worldScanner() {

        for (World world : Bukkit.getWorlds()) {

            if (ConfigValues.validWorldsConfig.getBoolean("Valid worlds." + world.getName())) {

                worldList.add(world);

            }

        }

    }

    public void repeatingTaskRunner() {

        MobScanner mobScanner = new MobScanner();
        PotionEffectApplier potionEffectApplier = new PotionEffectApplier();
        ScoreboardHandler scoreboardHandler = new ScoreboardHandler();

        new BukkitRunnable() {

            @Override
            public void run() {

                mobScanner.scanMobs();

            }

        }.runTaskTimer(this, 20, 20 * 10);

        new BukkitRunnable() {

            @Override
            public void run() {

                potionEffectApplier.potionEffectApplier();
                if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ENABLE_POWER_SCOREBOARDS)) {
                    scoreboardHandler.scanSight();
                }

            }

        }.runTaskTimer(this, 20, 20);

        if (ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.CHICKEN) &&
                ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT) > 0) {

            int eggTimerInterval = 20 * 60 * 10 / ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT);

            ChickenHandler chickenHandler = new ChickenHandler();

            new BukkitRunnable() {

                @Override
                public void run() {

                    //drops 1 egg for every loaded super chicken
                    chickenHandler.dropEggs();

                }

            }.runTaskTimer(this, eggTimerInterval, eggTimerInterval);

        }

        new BukkitRunnable() {

            @Override
            public void run() {

                DynamicLore.refreshDynamicLore();

            }

        }.runTaskTimer(this, 20, 20 * 2);

    }

}
