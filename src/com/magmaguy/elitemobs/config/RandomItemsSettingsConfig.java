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

        this.getRandomItemsSettingsConfig().addDefault("Drop random items on mob death", true);
        this.getRandomItemsSettingsConfig().addDefault("Monitor randomly generated drops on console", true);
        this.getRandomItemsSettingsConfig().addDefault("Percentage (%) of times random item drop instead of custom loot (assuming 50 items in that tier)", 50);
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

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.ARROW_DAMAGE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.ARROW_DAMAGE.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.ARROW_FIRE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.ARROW_FIRE.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.ARROW_INFINITE.Allow", true);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.ARROW_KNOCKBACK.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.ARROW_KNOCKBACK.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.BINDING_CURSE.Allow", true);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DAMAGE_ALL.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DAMAGE_ALL.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DAMAGE_ARTHROPODS.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DAMAGE_ARTHROPODS.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DAMAGE_UNDEAD.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DAMAGE_UNDEAD.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DEPTH_STRIDER.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DEPTH_STRIDER.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DIG_SPEED.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DIG_SPEED.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DURABILITY.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.DURABILITY.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.FIRE_ASPECT.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.FIRE_ASPECT.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.FROST_WALKER.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.FROST_WALKER.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.KNOCKBACK.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.KNOCKBACK.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LOOT_BONUS_BLOCKS.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LOOT_BONUS_BLOCKS.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LOOT_BONUS_MOBS.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LOOT_BONUS_MOBS.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LUCK.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LUCK.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LURE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.LURE.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.MENDING.Allow", false);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.OXYGEN.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.OXYGEN.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_ENVIRONMENTAL.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_ENVIRONMENTAL.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_EXPLOSIONS.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_EXPLOSIONS.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_FALL.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_FALL.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_FIRE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_FIRE.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_PROJECTILE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.PROTECTION_PROJECTILE.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.SILK_TOUCH.Allow", true);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.SWEEPING_EDGE.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.SWEEPING_EDGE.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.THORNS.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.THORNS.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.VANISHING_CURSE.Allow", true);

        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.WATER_WORKER.Allow", true);
        this.getRandomItemsSettingsConfig().addDefault("Valid Enchantments.WATER_WORKER.Max Level", 10);

        this.getRandomItemsSettingsConfig().addDefault("Valid nouns", Arrays.asList(
                "MagmaGuy",
                "Dawn",
                "Sunset",
                "Heaven",
                "Hell",
                "Angel",
                "Archangel",
                "Cherub",
                "Seraph",
                "Demon",
                "Fiend",
                "Ashen",
                "Bat",
                "Chicken",
                "Cow",
                "Mooshroom",
                "Pig",
                "Rabbit",
                "Sheep",
                "Squid",
                "Villager",
                "Cave Spider",
                "Enderman",
                "Polar Bear",
                "Spider",
                "Zombie Pigman",
                "Blaze",
                "Creeper",
                "Elder Guardian",
                "Endermite",
                "Evoker",
                "Ghast",
                "Guardian",
                "Husk",
                "Magma Cube",
                "Shulker",
                "Silverfish",
                "skeleton",
                "Slime",
                "Stray",
                "Vex",
                "Vindicator",
                "Witch",
                "Wither Skeleton",
                "Zombie",
                "Donkey",
                "Horse",
                "Llama",
                "Mule",
                "Ocelot",
                "Wolf",
                "Iron Golem",
                "Snow Golem",
                "Ender Dragon",
                "Wither",
                "Illusioner",
                "Parrot",
                "Giant",
                "Killer Bunny",
                "Steve",
                "Wolfbane",
                "Chaos",
                "Aeon",
                "Dragon",
                "Sphynx",
                "Minotaur",
                "Hades",
                "Oath",
                "Oathbreaker",
                "Deserter",
                "Betrayer",
                "Jade",
                "Light",
                "Dark",
                "Oblivion",
                "Shadow",
                "Void",
                "Sworn",
                "Xeno",
                "Zealot",
                "Zenith",
                "Unique",
                "Dusk",
                "Twilight",
                "Mob",
                "Elite  Mob"
        ));

        this.getRandomItemsSettingsConfig().addDefault("Valid adjectives", Arrays.asList(
                "Adorable",
                "Beautiful",
                "Clean",
                "Elegant",
                "Fancy",
                "Glamorous",
                "Handsome",
                "Long",
                "Magnificient",
                "Plain",
                "Quaint",
                "Sparkling",
                "Ugliest",
                "Red",
                "Orange",
                "Yellow",
                "Green",
                "Blue",
                "Purple",
                "Gray",
                "Black",
                "White",
                "Dead",
                "Brave",
                "Odd",
                "Powerful",
                "Rich",
                "Vast",
                "Zealous",
                "Deafening",
                "Faint",
                "Melodic",
                "Quiet",
                "Thundering",
                "Whispering",
                "Hissing",
                "Ancient",
                "Fast",
                "Old",
                "Rapid",
                "Slow",
                "Bitter",
                "Rotten",
                "Creepy",
                "Crooked",
                "Empty",
                "Heavy",
                "Abhorrent",
                "Abnormal",
                "Absurd",
                "Acceptable",
                "Accidental",
                "Adamant",
                "Adventurous",
                "Agonizing",
                "Alert",
                "Annoyed",
                "Annoying",
                "Anxious",
                "Apathetic",
                "Aromatic",
                ""
        ));

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
