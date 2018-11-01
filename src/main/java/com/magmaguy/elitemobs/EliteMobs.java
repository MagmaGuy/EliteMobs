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

/*
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.events.EventLauncher;
import com.magmaguy.elitemobs.items.CustomItemConstructor;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import com.magmaguy.elitemobs.items.uniqueitems.UniqueItemInitializer;
import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobpowers.majorpowers.SkeletonTrackingArrow;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.runnables.*;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.elitemobs.versionnotifier.VersionWarner;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class EliteMobs extends JavaPlugin {

    public static List<World> validWorldList = new ArrayList();

    @Override
    public void onEnable() {

        //Enable stats
        Metrics metrics = new Metrics(this);

        //Initialize custom enchantments
        CustomEnchantmentCache.initialize();

        //Load loot from config
        ConfigValues.intializeConfigurations();
        ConfigValues.initializeCachedConfigurations();

        //Parse loot
        CustomItemConstructor superDrops = new CustomItemConstructor();
        superDrops.superDropParser();

        UniqueItemInitializer.initialize();

        //Hook up all listeners, some depend on config
        EventsRegistrer.registerEvents();

        //Launch the local data cache
        PlayerData.initializePlayerData();
        PlayerData.synchronizeDatabases();

        //Get world list
        worldScanner();

        //Start the repeating tasks such as scanners
        launchRunnables();

        //Commands
        this.getCommand("elitemobs").setExecutor(new CommandHandler());

        //launch events
        EventLauncher eventLauncher = new EventLauncher();
        eventLauncher.eventRepeatingTask();

        //launch internal clock for attack cooldown
        CombatSystem.launchInternalClock();

        /*
        Initialize mob values
         */
        PluginMobProperties.initializePluginMobValues();

        /*
        Check for new plugin version
         */
        VersionChecker.updateComparer();
        if (!VersionChecker.pluginIsUpToDate)
            this.getServer().getPluginManager().registerEvents(new VersionWarner(), this);

        /*
        Metadata wiper repeating task
         */
        MetadataHandler.metadataWiper();

    }

    @Override
    public void onDisable() {

        /*
        Flush all elitemob entities
        This deletes all metadata and all aggressive mobs
         */
        for (World world : validWorldList)
            for (Entity entity : world.getEntities()) {
                MetadataHandler.fullMetadataFlush(entity);
                if (entity instanceof Player)
                    ((Player) entity).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }


        /*
        Flush lingering arrows from the arrow tracking power
         */
        for (Arrow arrow : SkeletonTrackingArrow.trackingArrowList)
            arrow.remove();
        SkeletonTrackingArrow.trackingArrowList.clear();

        CustomItemConstructor.customItemList.clear();
        CustomItemConstructor.staticCustomItemHashMap.clear();
        CustomItemConstructor.dynamicRankedItemStacks.clear();
        UniqueItemInitializer.uniqueItemsList.clear();
        validWorldList.clear();

        //save cached data
        Bukkit.getScheduler().cancelTask(PlayerData.databaseSyncTaskID);
        Bukkit.getLogger().info("[EliteMobs] Saving EliteMobs databases...");
        PlayerData.saveDatabases();
        Bukkit.getLogger().info("[EliteMobs] All saved! Good night.");
        PlayerData.clearPlayerData();

    }

    public void worldScanner() {

        for (World world : Bukkit.getWorlds())
            if (ConfigValues.validWorldsConfig.getBoolean("Valid worlds." + world.getName()))
                validWorldList.add(world);

    }

    /*
    Repeating tasks that run as long as the server is on
     */
    public void launchRunnables() {

        int eggTimerInterval = 20 * 60 * 10 / ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT);

        new EntityScanner().runTaskTimer(this, 20, 20 * 10);
        new PotionEffectApplier().runTaskTimer(this, 20, 20);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ENABLE_POWER_SCOREBOARDS))
            new ScoreboardUpdater().runTaskTimer(this, 20, 20);
        if (ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS) &&
                ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.CHICKEN) &&
                ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT) > 0)
            new EggRunnable().runTaskTimer(this, eggTimerInterval, eggTimerInterval);
        new DynamicLoreUpdater().runTaskTimer(this, 20, 20 * 2);
        new ReapplyDisplayEffects().runTaskTimer(this, 20, 20);
        EntityListUpdater.startUpdating();

    }

}
