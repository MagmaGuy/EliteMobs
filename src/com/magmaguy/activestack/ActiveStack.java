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

package com.magmaguy.activestack;

/**
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.activestack.Mobs.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ActiveStack extends JavaPlugin implements Listener {

    @Override
    public void onEnable(){

        getLogger().info("ActiveStack - Enabled!");

        worldScanner();
        repeatingTaskRunner();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new MobScanner(this), this);
        this.getServer().getPluginManager().registerEvents(new ZombieHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SkeletonHandler(this), this);
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

    }


    @Override
    public void onDisable(){

        getLogger().info("ActiveStack - Disabled!");

    }


    public static List<World> worldList = new ArrayList();

    public void worldScanner(){

        for (World world : Bukkit.getWorlds())
        {

            worldList.add(world);

        }

    }


    public void repeatingTaskRunner(){

        MobScanner mobScanner = new MobScanner(this);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {

                mobScanner.scanMobs();

            }

        }, 1, 1);

    }

}
