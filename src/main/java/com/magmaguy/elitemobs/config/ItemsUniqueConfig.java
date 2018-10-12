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

import com.magmaguy.elitemobs.items.uniqueitems.*;
import org.bukkit.configuration.Configuration;

public class ItemsUniqueConfig{

    public static final String CONFIG_NAME = "ItemsUnique.yml";
    private CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public static final String ENABLE_UNIQUE_ITEMS = "Enable unique items";
    public static final String ENABLE_HUNTING_SET = "Enable Mob Hunting set";
    public static final String HUNTING_SET_CHANCE_INCREASER = "EliteMob percentual spawn chance increase per hunting set item";
    public static final String ENABLE_ZOMBIE_KING_AXE = "Enable Zombie King Axe";
    public static final String SHOW_ITEM_WORTH = "Show item worth";
    public static final String ENABLE_KRAKEN_FISHING_ROD = "Enable Depths Seeker Fishing Rod";
    public static final String ENABLE_GREED = "Enable Dwarven Greed pickaxe";
    public static final String ENABLE_THE_FELLER = "Enable The Feller";

    public void initializeConfig() {

        configuration.addDefault(ENABLE_UNIQUE_ITEMS, true);
        configuration.addDefault(SHOW_ITEM_WORTH, true);
        configuration.addDefault(ENABLE_HUNTING_SET, true);
        configuration.addDefault(HUNTING_SET_CHANCE_INCREASER, 10);
        configuration.addDefault(ENABLE_ZOMBIE_KING_AXE, true);
        configuration.addDefault(ENABLE_KRAKEN_FISHING_ROD, true);
        configuration.addDefault(ENABLE_GREED, true);
        configuration.addDefault(ENABLE_THE_FELLER, true);

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

//        Non-static item assembly to avoid issues with making the configuration static (messes up reloads and changes)
        HuntingHelmet huntingHelmet = new HuntingHelmet();
        huntingHelmet.assembleConfigItem(configuration);

        HuntingChestplate huntingChestplate = new HuntingChestplate();
        huntingChestplate.assembleConfigItem(configuration);

        HuntingLeggings huntingLeggings = new HuntingLeggings();
        huntingLeggings.assembleConfigItem(configuration);

        HuntingBoots huntingBoots = new HuntingBoots();
        huntingBoots.assembleConfigItem(configuration);

        HuntingBow huntingBow = new HuntingBow();
        huntingBow.assembleConfigItem(configuration);

        ZombieKingsAxe zombieKingsAxe = new ZombieKingsAxe();
        zombieKingsAxe.assembleConfigItem(configuration);

        DepthsSeeker depthsSeeker = new DepthsSeeker();
        depthsSeeker.assembleConfigItem(configuration);

        DwarvenGreed dwarvenGreed = new DwarvenGreed();
        dwarvenGreed.assembleConfigItem(configuration);

        TheFeller theFeller = new TheFeller();
        theFeller.assembleConfigItem(configuration);

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
