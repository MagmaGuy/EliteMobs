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
import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class DefaultConfig {

    public static final String ALWAYS_SHOW_NAMETAGS = "Dangerous! Always show Elite Mob nametags";
    public static final String SUPERMOB_STACK_AMOUNT = "SuperMob (passive EliteMobs) stack amount";
    public static final String MMORPG_COLORS = "Use MMORPG colors for item ranks";
    public static final String MMORPG_COLORS_FOR_CUSTOM_ITEMS = "Use MMORPG colors for custom items";
    public static final String CREEPER_PASSIVE_DAMAGE_PREVENTER = "Prevent creepers from killing passive mobs";
    public static final String ENABLE_PERMISSION_TITLES = "Use titles to warn players they are missing a permission";
    public static final String ENABLE_POWER_SCOREBOARDS = "Use scoreboards to display mob powers using permissions";
    public static final String HIDE_ENCHANTMENTS_ATTRIBUTE = "Hide enchantment attributes on plugin-generated items";
    public static final String PREVENT_ITEM_PICKUP = "Prevent item pickup from all aggressive mobs";

    public static void reloadConfig() {

        Bukkit.getPluginManager().getPlugin("EliteMobs").reloadConfig();

    }

    public void loadConfiguration() {

        Configuration configuration = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig();

        configuration.addDefault(SUPERMOB_STACK_AMOUNT, 50);
        configuration.addDefault(MMORPG_COLORS, true);
        configuration.addDefault(MMORPG_COLORS_FOR_CUSTOM_ITEMS, true);
        configuration.addDefault(CREEPER_PASSIVE_DAMAGE_PREVENTER, true);
        configuration.addDefault(ENABLE_PERMISSION_TITLES, true);
        configuration.addDefault(ENABLE_POWER_SCOREBOARDS, false);
        configuration.addDefault(ALWAYS_SHOW_NAMETAGS, true);
        configuration.addDefault(HIDE_ENCHANTMENTS_ATTRIBUTE, false);
        configuration.addDefault(PREVENT_ITEM_PICKUP, false);

        configuration.options().copyDefaults(true);

        //save the config when changed
        Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).saveConfig();
        Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).saveDefaultConfig();

        Bukkit.getLogger().info("EliteMobs config loaded!");

    }

}
