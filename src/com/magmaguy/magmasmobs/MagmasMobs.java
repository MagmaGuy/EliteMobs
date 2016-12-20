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
import com.magmaguy.magmasmobs.collateralminecraftchanges.PreventSpawnerMobEggInteraction;
import com.magmaguy.magmasmobs.minorpowers.*;
import com.magmaguy.magmasmobs.mobs.aggressive.*;
import com.magmaguy.magmasmobs.mobs.passive.*;
import com.magmaguy.magmasmobs.mobscanner.MobScanner;
import com.magmaguy.magmasmobs.mobspawner.MobSpawner;
import com.magmaguy.magmasmobs.powerstances.ParticleEffects;
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
    public void onEnable(){

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
        this.getServer().getPluginManager().registerEvents(new SuperMobDamageHandler(this), this);

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
        if (getConfig().getBoolean("Natural SuperMob spawning"))
        {

            this.getServer().getPluginManager().registerEvents(new MobSpawner(this), this);

            getLogger().info("MagmasMobs - Natural SuperMob Spawning enabled!");

        }

        //Visual effects
        this.getServer().getPluginManager().registerEvents(new ParticleEffects(this), this);

        //Loot
        if (getConfig().getBoolean("Enable loot"))
        {

            getLogger().info("MagmasMobs - SuperMob loot enabled!");

            this.getServer().getPluginManager().registerEvents(new SuperDropsHandler(this), this);

        }

        //Minecraft behavior canceller
        if (getConfig().getBoolean("Prevent players from changing mob spawners using eggs"))
        {

            getLogger().info("MagmasMobs - Mob egg interact on mob spawner canceller enabled!");

            this.getServer().getPluginManager().registerEvents(new PreventSpawnerMobEggInteraction(this), this);

        }
        this.getServer().getPluginManager().registerEvents(new ChunkUnloadMetadataPurge(this), this);

    }


    @Override
    public void onDisable(){

        for (World world : worldList) {

            for (Entity entity : world.getEntities()) {

                if (entity.hasMetadata("MagmasSuperMob") ||
                        entity.hasMetadata("VisualEffect") ||
                        entity.hasMetadata("MagmasPassiveSupermob") ||
                        entity.hasMetadata("forbidden") ||
                        entity.hasMetadata("NaturalEntity"))
                {

                    Bukkit.getScheduler().cancelTask(processID);

                    entity.remove();
                    entity.removeMetadata("MagmasSuperMob", this);
                    entity.removeMetadata("VisualEffect", this);
                    entity.removeMetadata("NaturalEntity", this);
                    entity.removeMetadata("MagmasPassiveSupermob", this);
                    entity.removeMetadata("forbidden", this);

                }

            }

        }

        getLogger().info("MagmasMobs - Disabled!");

    }


    public static List<World> worldList = new ArrayList();

    public void worldScanner(){

        for (World world : Bukkit.getWorlds())
        {

            worldList.add(world);

        }

    }


    private int processID;

    public void repeatingTaskRunner(){

        MobScanner mobScanner = new MobScanner(this);

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {

                mobScanner.scanMobs();

            }

        }, 1, 1);

    }


    public void loadConfiguration()
    {

        //check defaults
        getConfig().addDefault("Natural SuperMob spawning", true);
        getConfig().addDefault("Prevent players from changing mob spawners using eggs", true);
        getConfig().addDefault("Enable loot", true);
        getConfig().addDefault("Loot.Zombie Slayer.Item Type", "DIAMOND_SWORD");
        getConfig().addDefault("Loot.Zombie Slayer.Item Name", "Zombie Slayer");
        getConfig().addDefault("Loot.Zombie Slayer.Item Lore", Arrays.asList("Slays zombies Bigly."));
        getConfig().addDefault("Loot.Zombie Slayer.Enchantments", Arrays.asList("DAMAGE_ALL,5", "DAMAGE_UNDEAD,5"));
        getConfig().addDefault("Loot.Zombie Slayer.Potion Effects", Arrays.asList("potions", "man"));
        getConfig().options().copyDefaults(true);

        //save the config when changed
        saveConfig();
        saveDefaultConfig();

        getLogger().info("MagmasMobs config loaded!");

    }


    public void reloadConfiguration()
    {

        reloadConfig();

        getLogger().info("MagmasMobs config reloaded!");

    }

}
