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

package com.magmaguy.magmasmobs;

/**
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.magmasmobs.collateralminecraftchanges.ChunkUnloadMetadataPurge;
import com.magmaguy.magmasmobs.collateralminecraftchanges.PreventCreeperPassiveEntityDamage;
import com.magmaguy.magmasmobs.collateralminecraftchanges.PreventSpawnerMobEggInteraction;
import com.magmaguy.magmasmobs.minorpowers.*;
import com.magmaguy.magmasmobs.mobcustomizer.DamageHandler;
import com.magmaguy.magmasmobs.mobs.aggressive.*;
import com.magmaguy.magmasmobs.mobs.passive.*;
import com.magmaguy.magmasmobs.mobscanner.MobScanner;
import com.magmaguy.magmasmobs.naturalmobspawner.NaturalMobMetadataAssigner;
import com.magmaguy.magmasmobs.naturalmobspawner.NaturalMobSpawner;
import com.magmaguy.magmasmobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.magmasmobs.superdrops.PotionEffectApplier;
import com.magmaguy.magmasmobs.superdrops.SuperDropsHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MagmasMobs extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        getLogger().info("MagmasMobs - Enabled!");

        //Load loot from config
        loadConfiguration();

        //Parse loot
        SuperDropsHandler superDrops = new SuperDropsHandler(this);
        superDrops.superDropParser();

        //Get world list
        worldScanner();

        //Start the repeating tasks such as scanners
        repeatingTaskRunner();

        //Hook up all listeners, some depend on config
        this.getServer().getPluginManager().registerEvents(this, this);

        //Aggressive mobs
        this.getServer().getPluginManager().registerEvents(new ZombieHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new HuskHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SkeletonHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new WitherSkeletonHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new StrayHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PigZombieHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new CreeperHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SpiderHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new EndermanHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new CaveSpiderHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SilverfishHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new BlazeHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new WitchHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new EndermiteHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PolarBearHandler(this), this);

        //Passive mobs
        this.getServer().getPluginManager().registerEvents(new ChickenHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new CowHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new IronGolemHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new MushroomCowHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PigHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SheepHandler(this), this);

        //Mob damage
        this.getServer().getPluginManager().registerEvents(new DamageHandler(this), this);

        //Mob powers
        this.getServer().getPluginManager().registerEvents(new AttackGravity(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackPush(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackPoison(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackWither(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityArrow(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityFire(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityFallDamage(this), this);

        //Mob scanner
        this.getServer().getPluginManager().registerEvents(new MobScanner(this), this);

        //Natural SuperMob Spawning
        if (getConfig().getBoolean("Natural SuperMob spawning")) {

            this.getServer().getPluginManager().registerEvents(new NaturalMobSpawner(this), this);

            getLogger().info("MagmasMobs - Natural SuperMob Spawning enabled!");

        }

        //Natural Mob Metadata Assigner
        this.getServer().getPluginManager().registerEvents(new NaturalMobMetadataAssigner(this), this);

        //Visual effects
        this.getServer().getPluginManager().registerEvents(new MinorPowerPowerStance(this), this);

        //Loot
        if (getConfig().getBoolean("Aggressive SuperMobs can drop config loot (level 5 SuperMobs and up)")) {

            this.getServer().getPluginManager().registerEvents(new SuperDropsHandler(this), this);

            getLogger().info("MagmasMobs - SuperMob loot enabled!");

        }

        //Minecraft behavior canceller
        if (getConfig().getBoolean("Prevent players from changing mob spawners using eggs")) {

            this.getServer().getPluginManager().registerEvents(new PreventSpawnerMobEggInteraction(this), this);

            getLogger().info("MagmasMobs - Mob egg interact on mob spawner canceller enabled!");

        }

        if (getConfig().getBoolean("Prevent creepers from killing passive mobs")) {

            this.getServer().getPluginManager().registerEvents(new PreventCreeperPassiveEntityDamage(this), this);

            getLogger().info("MagmasMobs - Creeper passive mob collateral damage canceller enabled!");

        }

        this.getServer().getPluginManager().registerEvents(new ChunkUnloadMetadataPurge(this), this);

        //Commands
        this.getCommand("magmasmobs").setExecutor(new CommandHandler(this));

    }


    @Override
    public void onDisable() {

        for (World world : worldList) {

            for (Entity entity : world.getEntities()) {

                if (entity.hasMetadata("MagmasSuperMob") ||
                        entity.hasMetadata("VisualEffect") ||
                        entity.hasMetadata("forbidden") ||
                        entity.hasMetadata("NaturalEntity")) {

                    Bukkit.getScheduler().cancelTask(processID);

                    entity.remove();
                    entity.removeMetadata("MagmasSuperMob", this);
                    entity.removeMetadata("VisualEffect", this);
                    entity.removeMetadata("NaturalEntity", this);
                    entity.removeMetadata("forbidden", this);

                }

                if (entity.hasMetadata("MagmasPassiveSupermob")) {

                    entity.removeMetadata("MagmasPassiveSupermob", this);

                }

            }

        }

        getLogger().info("MagmasMobs - Disabled!");

    }


    public static List<World> worldList = new ArrayList();

    public void worldScanner() {

        for (World world : Bukkit.getWorlds()) {

            worldList.add(world);

        }

    }


    private int processID;

    public void repeatingTaskRunner() {

        MobScanner mobScanner = new MobScanner(this);
        PotionEffectApplier potionEffectApplier = new PotionEffectApplier();

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {

                mobScanner.scanMobs();

            }

        }, 1, 1);

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {

                potionEffectApplier.potionEffectApplier();

            }

        }, 20, 20);


    }


    public void loadConfiguration() {

        //check defaults
        getConfig().addDefault("Allow aggressive SuperMobs", true);
        getConfig().addDefault("Valid aggressive SuperMobs",Arrays.asList("Blaze", "CaveSpider", "Creeper",
                "Enderman", "Endermite", "IronGolem", "PigZombie", "PolarBear", "Silverfish", "Skeleton",
                "Spider", "Witch", "Zombie"));
        getConfig().addDefault("Valid aggressive SuperMobs powers", Arrays.asList("AttackGravity", "AttackPoison",
                "AttackPush", "AttackWither", "InvulnerabilityArrow", "InvulnerabilityFallDamage", "InvulnerabilityFire", "MovementSpeed"));
        getConfig().addDefault("Allow Passive SuperMobs", true);
        getConfig().addDefault("Valid Passive SuperMobs", Arrays.asList("Chicken", "Cow", "MushroomCow",
                "Pig", "Sheep"));
        getConfig().addDefault("Natural aggressive SuperMob spawning", true);
        getConfig().addDefault("Percentage (%) of aggressive mobs that get converted to SuperMobs when they spawn", 20);
        getConfig().addDefault("Aggressive mob stacking", true);
        getConfig().addDefault("Aggressive SuperMobs can drop config loot (level 5 SuperMobs and up)", true);
        getConfig().addDefault("Aggressive SuperMobs flat loot drop rate %", 50);
        getConfig().addDefault("Aggressive SuperMobs can drop additional loot with drop % based on SuperMob level (higher is more likely)", true);
        getConfig().addDefault("Prevent players from changing mob spawners using eggs", true);
        getConfig().addDefault("Prevent creepers from killing passive mobs", true);
        getConfig().addDefault("SuperCreeper explosion nerf multiplier", 1.0);
        getConfig().addDefault("Turn on visual effects for natural or plugin-spawned SuperMobs", true);
        getConfig().addDefault("Turn off visual effects for non-natural or non-plugin-spawned SuperMobs", true);
        getConfig().addDefault("Loot.Zombie Slayer.Item Type", "DIAMOND_SWORD");
        getConfig().addDefault("Loot.Zombie Slayer.Item Name", "Zombie Slayer");
        getConfig().addDefault("Loot.Zombie Slayer.Item Lore", Arrays.asList("Slays zombies Bigly."));
        getConfig().addDefault("Loot.Zombie Slayer.Enchantments", Arrays.asList("DAMAGE_ALL,5", "DAMAGE_UNDEAD,5"));
        getConfig().addDefault("Loot.Zombie Slayer.Potion Effects", Arrays.asList("GLOWING,1"));
        getConfig().options().copyDefaults(true);

        //save the config when changed
        saveConfig();
        saveDefaultConfig();

        getLogger().info("MagmasMobs config loaded!");

    }


    public void reloadConfiguration() {

        reloadConfig();

        getLogger().info("MagmasMobs config reloaded!");

    }

}
