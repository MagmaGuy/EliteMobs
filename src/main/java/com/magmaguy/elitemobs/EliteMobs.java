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

import com.magmaguy.elitemobs.collateralminecraftchanges.ChunkUnloadMetadataPurge;
import com.magmaguy.elitemobs.collateralminecraftchanges.EntityDeathMetadataFlusher;
import com.magmaguy.elitemobs.collateralminecraftchanges.OfflinePlayerCacher;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventCreeperPassiveEntityDamage;
import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.commands.CustomShopHandler;
import com.magmaguy.elitemobs.commands.LootGUI;
import com.magmaguy.elitemobs.commands.ShopHandler;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.elitedrops.EliteDropsDropper;
import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import com.magmaguy.elitemobs.elitedrops.PotionEffectApplier;
import com.magmaguy.elitemobs.mobcustomizer.DamageHandler;
import com.magmaguy.elitemobs.mobcustomizer.DropsHandler;
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
        MobPowersCustomConfig mobPowersCustomConfig = new MobPowersCustomConfig();
        mobPowersCustomConfig.initializeMobPowersConfig();
        LootCustomConfig lootCustomConfig = new LootCustomConfig();
        lootCustomConfig.LootCustomConfig();
        TranslationCustomConfig translationCustomConfig = new TranslationCustomConfig();
        translationCustomConfig.initializeTranslationConfig();
        RandomItemsSettingsConfig randomItemsSettingsConfig = new RandomItemsSettingsConfig();
        randomItemsSettingsConfig.initializeRandomItemsSettingsConfig();
        EconomySettingsConfig economySettingsConfig = new EconomySettingsConfig();
        economySettingsConfig.intializeEconomySettingsConfig();
        PlayerMoneyDataConfig playerMoneyDataConfig = new PlayerMoneyDataConfig();
        playerMoneyDataConfig.intializeEconomySettingsConfig();
        ConfigValues configValues = new ConfigValues();
        configValues.initializeConfigValues();

        //Parse loot
        EliteDropsHandler superDrops = new EliteDropsHandler();
        superDrops.superDropParser();

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
        this.getServer().getPluginManager().registerEvents(new DamageHandler(this), this);

        //Mob loot
        this.getServer().getPluginManager().registerEvents(new DropsHandler(), this);

        //getloot GUI
        this.getServer().getPluginManager().registerEvents(new LootGUI(), this);

        //Minor mob powers
        for (String string : MetadataHandler.minorPowerList()){

            //don't load powers that require no event listeners
            if (!(string.equalsIgnoreCase("MovementSpeed"))
                    && !(string.equalsIgnoreCase("Invisibility"))
                    && !(string.equalsIgnoreCase("DoubleHealth"))
                    && !(string.equalsIgnoreCase("DoubleDamage"))) {

                try {

                    String earlypath = "com.magmaguy.elitemobs.minorpowers.";
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

        for (String string : MetadataHandler.majorPowerList()){

            //don't load powers that require no event listeners
                try {

                    String earlypath = "com.magmaguy.elitemobs.majorpowers.";
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
        if (ConfigValues.defaultConfig.getBoolean("Natural EliteMob spawning")) {

            this.getServer().getPluginManager().registerEvents(new NaturalMobSpawner(this), this);

            getLogger().info("EliteMobs - Natural EliteMob Spawning enabled!");

        }

        //Natural Mob Metadata Assigner
        this.getServer().getPluginManager().registerEvents(new NaturalMobMetadataAssigner(this), this);

        //Visual effects
        this.getServer().getPluginManager().registerEvents(new EffectEventHandlers(), this);
        this.getServer().getPluginManager().registerEvents(new MinorPowerPowerStance(), this);
        this.getServer().getPluginManager().registerEvents(new MajorPowerPowerStance(), this);

        //Loot
        this.getServer().getPluginManager().registerEvents(new EliteDropsDropper(), this);

        //Shops
        this.getServer().getPluginManager().registerEvents(new ShopHandler(), this);
        this.getServer().getPluginManager().registerEvents(new CustomShopHandler(), this);

        //Minecraft behavior canceller
        if (ConfigValues.defaultConfig.getBoolean("Prevent creepers from killing passive mobs")) {

            this.getServer().getPluginManager().registerEvents(new PreventCreeperPassiveEntityDamage(this), this);

            getLogger().info("EliteMobs - Creeper passive mob collateral damage canceller enabled!");

        }

        this.getServer().getPluginManager().registerEvents(new ChunkUnloadMetadataPurge(this), this);
        this.getServer().getPluginManager().registerEvents(new EntityDeathMetadataFlusher(), this);

        //Commands
        this.getCommand("elitemobs").setExecutor(new CommandHandler());

    }

    @Override
    public void onDisable() {

        MetadataHandler metadataHandler = new MetadataHandler();

        for (World world : worldList) {

            for (Entity entity : world.getEntities()) {

                metadataHandler.flushMetadata(entity);

                if (entity instanceof Player) {

                    ((Player) entity).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

                }

            }

        }

        getLogger().info("EliteMobs - Disabled!");

    }

    public void worldScanner() {

        for (World world : Bukkit.getWorlds()) {

            if (getConfig().getBoolean("Valid worlds." + world.getName().toString())) {

                worldList.add(world);

            }

        }

    }

    public void repeatingTaskRunner() {

        //eggs need to scale with stacked amount
        int passiveStackAmount = ConfigValues.defaultConfig.getInt("Passive EliteMob stack amount");

        MobScanner mobScanner = new MobScanner();
        PotionEffectApplier potionEffectApplier = new PotionEffectApplier();
        ScoreboardHandler scoreboardHandler = new ScoreboardHandler();

        new BukkitRunnable() {

            @Override
            public void run() {

                mobScanner.scanMobs(passiveStackAmount);

            }

        }.runTaskTimer(this, 20, 20 * 10);

        new BukkitRunnable() {

            @Override
            public void run() {

                potionEffectApplier.potionEffectApplier();
                if (ConfigValues.defaultConfig.getBoolean("Use scoreboards (requires permission)")) {
                    scoreboardHandler.scanSight();
                }

            }

        }.runTaskTimer(this, 20, 20);

        new BukkitRunnable() {

            @Override
            public void run() {

                //fixed drop rate for super chicken eggs
                ChickenHandler.dropEggs();

            }

        }.runTaskTimer(this, 20 * 60 * 10, 20 * 60 * 10);

    }

}
