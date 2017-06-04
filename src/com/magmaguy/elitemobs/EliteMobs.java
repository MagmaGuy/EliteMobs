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
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventCreeperPassiveEntityDamage;
import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.commands.LootGUI;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.LootCustomConfig;
import com.magmaguy.elitemobs.config.MobPowersCustomConfig;
import com.magmaguy.elitemobs.config.TranslationCustomConfig;
import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import com.magmaguy.elitemobs.elitedrops.PotionEffectApplier;
import com.magmaguy.elitemobs.mobcustomizer.DamageHandler;
import com.magmaguy.elitemobs.mobcustomizer.DropsHandler;
import com.magmaguy.elitemobs.mobscanner.MobScanner;
import com.magmaguy.elitemobs.naturalmobspawner.NaturalMobMetadataAssigner;
import com.magmaguy.elitemobs.naturalmobspawner.NaturalMobSpawner;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.scoreboard.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class EliteMobs extends JavaPlugin implements Listener {

    public static List<World> worldList = new ArrayList();
    private int processID;

    @Override
    public void onEnable() {

        getLogger().info("EliteMobs - Enabled!");

        //Load loot from config
        loadConfiguration();
        MobPowersCustomConfig mobPowersCustomConfig = new MobPowersCustomConfig();
        mobPowersCustomConfig.initializeMobPowersConfig();
        LootCustomConfig lootCustomConfig = new LootCustomConfig();
        lootCustomConfig.LootCustomConfig();
        TranslationCustomConfig translationCustomConfig = new TranslationCustomConfig();
        translationCustomConfig.initializeTranslationConfig();
        ConfigValues configValues = new ConfigValues();
        configValues.initializeConfigValues();

        //Parse loot
        EliteDropsHandler superDrops = new EliteDropsHandler();
        superDrops.superDropParser();

        //Get world list
        worldScanner();

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

            //don't load powers that require no even listeners
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

            //don't load powers that require no even listeners
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

        //Mob scanner
        this.getServer().getPluginManager().registerEvents(new MobScanner(this), this);

        //Natural EliteMobs Spawning
        if (ConfigValues.defaultConfig.getBoolean("Natural EliteMob spawning")) {

            this.getServer().getPluginManager().registerEvents(new NaturalMobSpawner(this), this);

            getLogger().info("EliteMobs - Natural EliteMob Spawning enabled!");

        }

        //Natural Mob Metadata Assigner
        this.getServer().getPluginManager().registerEvents(new NaturalMobMetadataAssigner(this), this);

        //Visual effects
        this.getServer().getPluginManager().registerEvents(new MinorPowerPowerStance(), this);
        this.getServer().getPluginManager().registerEvents(new MajorPowerPowerStance(), this);

        //Loot
        if (ConfigValues.defaultConfig.getBoolean("Aggressive EliteMobs can drop config loot (level 5 EliteMobs and up)")) {

            this.getServer().getPluginManager().registerEvents(new EliteDropsHandler(), this);

            getLogger().info("EliteMobs - EliteMob loot enabled!");

        }

        //Minecraft behavior canceller
        if (ConfigValues.defaultConfig.getBoolean("Prevent creepers from killing passive mobs")) {

            this.getServer().getPluginManager().registerEvents(new PreventCreeperPassiveEntityDamage(this), this);

            getLogger().info("EliteMobs - Creeper passive mob collateral damage canceller enabled!");

        }

        this.getServer().getPluginManager().registerEvents(new ChunkUnloadMetadataPurge(this), this);

        //Commands
        this.getCommand("elitemobs").setExecutor(new CommandHandler(this));

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

        MobScanner mobScanner = new MobScanner(this);
        PotionEffectApplier potionEffectApplier = new PotionEffectApplier();
        ScoreboardHandler scoreboardHandler = new ScoreboardHandler();

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {

                mobScanner.scanMobs(passiveStackAmount);
                potionEffectApplier.potionEffectApplier();
                scoreboardHandler.scanSight();

            }

        }, 20, 20);


    }

    public void loadConfiguration() {

        //check defaults
        getConfig().addDefault("Allow aggressive EliteMobs", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Blaze", true);
        getConfig().addDefault("Valid aggressive EliteMobs.CaveSpider", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Creeper", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Enderman", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Endermite", true);
        getConfig().addDefault("Valid aggressive EliteMobs.IronGolem", true);
        getConfig().addDefault("Valid aggressive EliteMobs.PigZombie", true);
        getConfig().addDefault("Valid aggressive EliteMobs.PolarBear", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Silverfish", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Skeleton", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Spider", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Witch", true);
        getConfig().addDefault("Valid aggressive EliteMobs.Zombie", true);
        getConfig().addDefault("Allow Passive EliteMobs", true);
        getConfig().addDefault("Valid passive EliteMobs.Chicken", true);
        getConfig().addDefault("Valid passive EliteMobs.Cow", true);
        getConfig().addDefault("Valid passive EliteMobs.MushroomCow", true);
        getConfig().addDefault("Valid passive EliteMobs.Pig", true);
        getConfig().addDefault("Valid passive EliteMobs.Sheep", true);
        getConfig().addDefault("Natural aggressive EliteMob spawning", true);

        for (World world : Bukkit.getWorlds()) {

            getConfig().addDefault("Valid worlds." + world.getName().toString(), true);

        }

        getConfig().addDefault("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn", 20);
        getConfig().addDefault("Aggressive mob stacking", true);
        getConfig().addDefault("Aggressive mob stacking cap", 50);
        getConfig().addDefault("Stack aggressive spawner mobs", true);
        getConfig().addDefault("Stack aggressive natural mobs", true);
        getConfig().addDefault("Passive EliteMob stack amount", 50);
        getConfig().addDefault("Aggressive EliteMobs can drop config loot (level 5 EliteMobs and up)", true);
        getConfig().addDefault("Aggressive EliteMobs flat loot drop rate %", 75);
        getConfig().addDefault("Aggressive EliteMobs can drop additional loot with drop % based on EliteMobs level (higher is more likely)", true);
        getConfig().addDefault("Prevent creepers from killing passive mobs", true);
        getConfig().addDefault("Aggressive EliteMob life multiplier", 1.0);
        getConfig().addDefault("Aggressive EliteMob damage multiplier", 1.0);
        getConfig().addDefault("Aggressive EliteMob default loot multiplier", 1.0);
        getConfig().addDefault("SuperCreeper explosion nerf multiplier", 1.0);
        getConfig().addDefault("Turn on visual effects for natural or plugin-spawned EliteMobs", true);
        getConfig().addDefault("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs", true);
        getConfig().addDefault("Turn on visual effects that indicate an attack is about to happen", true);
        getConfig().addDefault("Use titles to warn players they are missing a permission", true);
        getConfig().options().copyDefaults(true);

        //save the config when changed
        saveConfig();
        saveDefaultConfig();

        getLogger().info("EliteMobs config loaded!");

    }

    public void reloadConfiguration() {

        reloadConfig();
        MobPowersCustomConfig mobPowersCustomConfig = new MobPowersCustomConfig();
        mobPowersCustomConfig.reloadCustomConfig();
        LootCustomConfig lootCustomConfig = new LootCustomConfig();
        lootCustomConfig.reloadLootConfig();
        TranslationCustomConfig translationCustomConfig = new TranslationCustomConfig();
        translationCustomConfig.reloadCustomConfig();
        ConfigValues configValues = new ConfigValues();
        configValues.initializeConfigValues();

        getLogger().info("EliteMobs config reloaded!");

    }

}
