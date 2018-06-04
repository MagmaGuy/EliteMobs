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

import org.bukkit.configuration.Configuration;

public class ItemsUniqueConfig {

    public static final String CONFIG_NAME = "ItemsUnique.yml";
    public static final String ENABLE_UNIQUE_ITEMS = "Enable unique items";
    public static final String ENABLE_HUNTING_SET = "Enable Mob Hunting set";
    public static final String HUNTING_SET_CHANCE_INCREASER = "EliteMob percentual spawn chance increase per hunting set item";
    public static final String HUNTING_SET_HELMET = "Hunting Helmet";
    public static final String HUNTING_SET_CHESTPLATE = "Hunting Chestplate";
    public static final String HUNTING_SET_LEGGINGS = "Hunting Leggings";
    public static final String HUNTING_SET_BOOTS = "Hunting Boots";
    public static final String HUNTING_SET_BOW = "Hunting Bow";
    public static final String ENABLE_ZOMBIE_KING_AXE = "Enable Zombie King Axe";
    public static final String ZOMBIE_KING_AXE = "Zombie King Axe";
    public static final String SHOW_ITEM_WORTH = "Show item worth";
    public static final String ENABLE_KRAKEN_FISHING_ROD = "Enable Depths Seeker Fishing Rod";
    public static final String DEPTHS_SEEKER = "Depths Seeker";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_UNIQUE_ITEMS, true);
        configuration.addDefault(SHOW_ITEM_WORTH, true);
        configuration.addDefault(ENABLE_HUNTING_SET, true);
        configuration.addDefault(HUNTING_SET_CHANCE_INCREASER, 10);
        configuration.addDefault(HUNTING_SET_HELMET, "\n" +
                "    Item Type: IRON_HELMET\n" +
                "    Item Name: &4Elite Mob Hunting Helmet\n" +
                "    Item Lore:\n" +
                "    - Wearing this helmet will\n" +
                "    - increase the number of\n" +
                "    - high level Elite Mobs\n" +
                "    - that spawn around you!\n" +
                "    Enchantments:\n" +
                "    - VANISHING_CURSE,1\n" +
                "    Potion Effects:\n" +
                "    - NIGHT_VISION,1,self,continuous\n" +
                "    Drop Weight: 1");
        configuration.addDefault(HUNTING_SET_CHESTPLATE, "\n" +
                "    Item Type: IRON_CHESTPLATE\n" +
                "    Item Name: &4Elite Mob Hunting Chestplate\n" +
                "    Item Lore:\n" +
                "    - Wearing this chestplate will\n" +
                "    - increase the number of\n" +
                "    - high level Elite Mobs\n" +
                "    - that spawn around you!\n" +
                "    - Only for the bravest souls.\n" +
                "    Enchantments:\n" +
                "    - VANISHING_CURSE,1\n" +
                "    Potion Effects:\n" +
                "    - SATURATION,1,self,continuous\n" +
                "    Drop Weight: 1");
        configuration.addDefault(HUNTING_SET_LEGGINGS, "\n" +
                "    Item Type: IRON_LEGGINGS\n" +
                "    Item Name: &4Elite Mob Hunting Leggings\n" +
                "    Item Lore:\n" +
                "    - Wearing these leggings will\n" +
                "    - increase the number of\n" +
                "    - high level Elite Mobs\n" +
                "    - that spawn around you!\n" +
                "    - Only for those who aim the highest.\n" +
                "    Enchantments:\n" +
                "    - VANISHING_CURSE,1\n" +
                "    Potion Effects:\n" +
                "    - JUMP,2,self,continuous\n" +
                "    Drop Weight: 1");
        configuration.addDefault(HUNTING_SET_BOOTS, "\n" +
                "    Item Type: IRON_BOOTS\n" +
                "    Item Name: &4Elite Mob Hunting Boots\n" +
                "    Item Lore:\n" +
                "    - Wearing these boots will\n" +
                "    - increase the number of\n" +
                "    - high level Elite Mobs\n" +
                "    - that spawn around you!\n" +
                "    - Only for those fleetest of foot.\n" +
                "    Enchantments:\n" +
                "    - VANISHING_CURSE,1\n" +
                "    Potion Effects:\n" +
                "    - SPEED,2,self,continuous\n" +
                "    Drop Weight: 1");
        configuration.addDefault(HUNTING_SET_BOW, "\n" +
                "    Item Type: BOW\n" +
                "    Item Name: &4Elite Mob Hunting Bow\n" +
                "    Item Lore:\n" +
                "    - Wielding this bow will\n" +
                "    - increase the number of\n" +
                "    - high level Elite Mobs\n" +
                "    - that spawn around you!\n" +
                "    - Only natural-born hunters.\n" +
                "    Enchantments:\n" +
                "    - VANISHING_CURSE,1\n" +
                "    - ARROW_DAMAGE,3\n" +
                "    Potion Effects:\n" +
                "    - LEVITATION,1,target,onHit\n" +
                "    Drop Weight: 1");
        configuration.addDefault(ENABLE_ZOMBIE_KING_AXE, true);
        configuration.addDefault(ZOMBIE_KING_AXE, "\n" +
                "    Item Type: GOLD_AXE\n" +
                "    Item Name: &4Zombie King's Axe\n" +
                "    Item Lore:\n" +
                "    - The axe of the one Zombies\n" +
                "    - call their king.\n" +
                "    - The bloodshed is palpable.\n" +
                "    Enchantments:\n" +
                "    - DAMAGE_ALL,10\n" +
                "    - DAMAGE_UNDEAD,5\n" +
                "    - DIG_SPEED,5\n" +
                "    - DURABILITY,5\n" +
                "    - KNOCKBACK,3\n" +
                "    - FIRE_ASPECT,4\n" +
                "    - LOOT_BONUS_MOBS,5\n" +
                "    - WATER_WORKER,5\n" +
                "    Potion Effects:\n" +
                "    - FAST_DIGGING,1,self,onHit\n" +
                "    - POISON,1,target,onHit\n" +
                "    - GLOWING,1,target,onHit\n" +
                "    - NIGHT_VISION,1,target,continuous\n" +
                "    - WITHER,1,target,onHit\n" +
                "    Drop Weight: 1");
        configuration.addDefault(ENABLE_KRAKEN_FISHING_ROD, true);
        configuration.addDefault(DEPTHS_SEEKER, "\n" +
                "    Item Type: FISHING_ROD\n" +
                "    Item Name: &cDepths Seeker\n" +
                "    Item Lore:\n" +
                "    - &9Come from depths immeasurable\n" +
                "    - &9and looted from a vile monster,\n" +
                "    - &9there is no telling what horrors\n" +
                "    - &9this fishing rod has seen.\n" +
                "    Enchantments:\n" +
                "    - LURE,3\n" +
                "    - LUCK,3\n" +
                "    - DURABILITY,10\n" +
                "    - FIRE_ASPECT,1\n" +
                "    Potion Effects:\n" +
                "    - WATER_BREATHING,1,self,continuous\n" +
                "    - LUCK,1,self,continuous\n" +
                "    Drop Weight: 1");

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
