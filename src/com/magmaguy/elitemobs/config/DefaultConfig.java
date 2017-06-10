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

package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class DefaultConfig {

    public void loadConfiguration() {

        Configuration configuration = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig();

        //check defaults
        configuration.addDefault("Allow aggressive EliteMobs", true);
        configuration.addDefault("Valid aggressive EliteMobs.Blaze", true);
        configuration.addDefault("Valid aggressive EliteMobs.CaveSpider", true);
        configuration.addDefault("Valid aggressive EliteMobs.Creeper", true);
        configuration.addDefault("Valid aggressive EliteMobs.Enderman", true);
        configuration.addDefault("Valid aggressive EliteMobs.Endermite", true);
        configuration.addDefault("Valid aggressive EliteMobs.IronGolem", true);
        configuration.addDefault("Valid aggressive EliteMobs.PigZombie", true);
        configuration.addDefault("Valid aggressive EliteMobs.PolarBear", true);
        configuration.addDefault("Valid aggressive EliteMobs.Silverfish", true);
        configuration.addDefault("Valid aggressive EliteMobs.Skeleton", true);
        configuration.addDefault("Valid aggressive EliteMobs.Spider", true);
        configuration.addDefault("Valid aggressive EliteMobs.Witch", true);
        configuration.addDefault("Valid aggressive EliteMobs.Zombie", true);
        configuration.addDefault("Allow Passive EliteMobs", true);
        configuration.addDefault("Valid passive EliteMobs.Chicken", true);
        configuration.addDefault("Valid passive EliteMobs.Cow", true);
        configuration.addDefault("Valid passive EliteMobs.MushroomCow", true);
        configuration.addDefault("Valid passive EliteMobs.Pig", true);
        configuration.addDefault("Valid passive EliteMobs.Sheep", true);
        configuration.addDefault("Natural aggressive EliteMob spawning", true);

        for (World world : Bukkit.getWorlds()) {

            configuration.addDefault("Valid worlds." + world.getName().toString(), true);

        }

        configuration.addDefault("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn", 20);
        configuration.addDefault("Aggressive mob stacking", true);
        configuration.addDefault("Aggressive mob stacking cap", 50);
        configuration.addDefault("Stack aggressive spawner mobs", true);
        configuration.addDefault("Stack aggressive natural mobs", true);
        configuration.addDefault("Passive EliteMob stack amount", 50);
        configuration.addDefault("Aggressive EliteMobs can drop custom loot", true);
        configuration.addDefault("Aggressive EliteMobs flat loot drop rate %", 25);
        configuration.addDefault("Aggressive EliteMobs drop rate % increase per mob level", 1);
        configuration.addDefault("Use MMORPG colors for item ranks", true);
        configuration.addDefault("Increase MMORPG color rank every X levels X=", 15);
        configuration.addDefault("Prevent creepers from killing passive mobs", true);
        configuration.addDefault("Aggressive EliteMob life multiplier", 1.0);
        configuration.addDefault("Aggressive EliteMob damage multiplier", 1.0);
        configuration.addDefault("Aggressive EliteMob default loot multiplier", 1.0);
        configuration.addDefault("Drop multiplied default loot from aggressive elite mobs spawned in spawners", true);
        configuration.addDefault("SuperCreeper explosion nerf multiplier", 1.0);
        configuration.addDefault("Turn on visual effects for natural or plugin-spawned EliteMobs", true);
        configuration.addDefault("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs", true);
        configuration.addDefault("Turn on visual effects that indicate an attack is about to happen", true);
        configuration.addDefault("Use titles to warn players they are missing a permission", true);
        configuration.options().copyDefaults(true);

        //save the config when changed
        Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).saveConfig();
        Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).saveDefaultConfig();

        Bukkit.getLogger().info("EliteMobs config loaded!");

    }

}
