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

import com.magmaguy.magmasmobs.MinorPowers.*;
import com.magmaguy.magmasmobs.MobScanner.MobScanner;
import com.magmaguy.magmasmobs.MobSpawner.MobSpawner;
import com.magmaguy.magmasmobs.Mobs.*;
import com.magmaguy.magmasmobs.PowerStances.ParticleEffects;
import com.magmaguy.magmasmobs.SuperDrops.SuperDropsParser;
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

        loadConfiguration();
        SuperDropsParser superDrops = new SuperDropsParser(this);
        superDrops.superDropParser();

        getLogger().info(superDrops.lootList + "");

        worldScanner();
        repeatingTaskRunner();

        this.getServer().getPluginManager().registerEvents(this, this);

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

        this.getServer().getPluginManager().registerEvents(new SuperMobDamageHandler(this), this);

        this.getServer().getPluginManager().registerEvents(new AttackGravity(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackPush(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityArrow(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityFire(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityFallDamage(this), this);

        this.getServer().getPluginManager().registerEvents(new MobScanner(this), this);
        this.getServer().getPluginManager().registerEvents(new MobSpawner(this), this);

        this.getServer().getPluginManager().registerEvents(new ParticleEffects(this), this);

        this.getServer().getPluginManager().registerEvents(new SuperDropsParser(this), this);

    }


    @Override
    public void onDisable(){

        for (World world : worldList) {

            for (Entity entity : world.getEntities()) {

                if (entity.hasMetadata("MagmasSuperMob") ||
                        entity.hasMetadata("VisualEffect") ||
                        entity.hasMetadata("removed") ||
                        entity.hasMetadata("forbidden"))
                {

                    Bukkit.getScheduler().cancelTask(processID);

                    entity.remove();

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
