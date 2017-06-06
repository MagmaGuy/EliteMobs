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
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class RandomItemsSettingsConfig {

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    public void initializeRandomItemsSettingsConfig () {

        MetadataHandler metadataHandler = new MetadataHandler();

        this.getRandomItemsSettingsConfig().addDefault("Drop random items on mob death", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid material list for random items", Arrays.asList(
                "DIAMOND_SWORD",
                "GOLD_SWORD",
                "IRON_SWORD",
                "STONE_SWORD",
                "WOOD_SWORD",
                "BOW",
                "DIAMOND_PICKAXE",
                "GOLD_PICKAXE",
                "IRON_PICKAXE",
                "STONE_PICKAXE",
                "WOOD_PICKAXE",
                "DIAMOND_SPADE",
                "GOLD_SPADE",
                "IRON_SPADE",
                "STONE_SPADE",
                "WOOD_SPADE",
                "DIAMOND_HOE",
                "GOLD_HOE",
                "IRON_HOE",
                "STONE_HOE",
                "WOOD_HOE",
                "DIAMOND_AXE",
                "GOLD_AXE",
                "IRON_AXE",
                "STONE_AXE",
                "WOOD_AXE",
                "CHAINMAIL_HELMET",
                "DIAMOND_HELMET",
                "GOLD_HELMET",
                "IRON_HELMET",
                "LEATHER_HELMET",
                "CHAINMAIL_CHESTPLATE",
                "DIAMOND_CHESTPLATE",
                "GOLD_CHESTPLATE",
                "IRON_CHESTPLATE",
                "LEATHER_CHESTPLATE",
                "CHAINMAIL_LEGGINGS",
                "DIAMOND_LEGGINGS",
                "GOLD_LEGGINGS",
                "IRON_LEGGINGS",
                "LEATHER_LEGGINGS",
                "CHAINMAIL_BOOTS",
                "DIAMOND_BOOTS",
                "GOLD_BOOTS",
                "IRON_BOOTS",
                "LEATHER_BOOTS"
        ));

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_DAMAGE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_DAMAGE.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_FIRE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_FIRE.Max Level", 1);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_INFINITE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_INFINITE.Max Level", 1);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_KNOCKBACK.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_KNOCKBACK.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_FIRE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_FIRE.Max Level", 1);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.BINDING_CURSE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.BINDING_CURSE.Max Level", 1);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_FIRE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchanments.ARROW_FIRE.Max Level", 1);

        getRandomItemsSettingsConfig().options().copyDefaults(true);
        saveDefaultCustomConfig();
        saveCustomConfig();

    }

    public FileConfiguration getRandomItemsSettingsConfig() {

        return customConfigLoader.getCustomConfig("randomItemsSettings.yml");

    }

    public void reloadCustomConfig() {

        customConfigLoader.reloadCustomConfig("randomItemsSettings.yml");

    }

    public void saveCustomConfig() {

        customConfigLoader.saveCustomDefaultConfig("randomItemsSettings.yml");

    }

    public void saveDefaultCustomConfig() {

        customConfigLoader.saveDefaultCustomConfig("randomItemsSettings.yml");

    }

}
