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

package com.magmaguy.elitemobs.commands.guiconfig.configurers;

import com.magmaguy.elitemobs.commands.guiconfig.GUIConfigHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidMobsConfigurer implements Listener {

    public static void validMobsPickerPopulator(Inventory inventory) {

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = GUIConfigHandler.itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

        populateValidMobs(inventory);

    }

    private static Boolean blaze, caveSpider, creeper, enderman, endermite, ironGolem, pigZombie, polarBear, silverfish,
            skeleton, spider, witch, zombie, chicken, cow, mushroomCow, pig, sheep, aggressive, passive;

    private static void configStatusChecker() {

        blaze = aggressiveBooleanConfigGrabber("Blaze");
        caveSpider = aggressiveBooleanConfigGrabber("CaveSpider");
        creeper = aggressiveBooleanConfigGrabber("Creeper");
        enderman = aggressiveBooleanConfigGrabber("Enderman");
        endermite = aggressiveBooleanConfigGrabber("Endermite");
        ironGolem = aggressiveBooleanConfigGrabber("IronGolem");
        pigZombie = aggressiveBooleanConfigGrabber("PigZombie");
        polarBear = aggressiveBooleanConfigGrabber("PolarBear");
        silverfish = aggressiveBooleanConfigGrabber("Silverfish");
        skeleton = aggressiveBooleanConfigGrabber("Skeleton");
        spider = aggressiveBooleanConfigGrabber("Spider");
        witch = aggressiveBooleanConfigGrabber("Witch");
        zombie = aggressiveBooleanConfigGrabber("Zombie");

        chicken = passiveBooleanConfigGrabber("Chicken");
        cow = passiveBooleanConfigGrabber("Cow");
        mushroomCow = passiveBooleanConfigGrabber("MushroomCow");
        pig = passiveBooleanConfigGrabber("Pig");
        sheep = passiveBooleanConfigGrabber("Sheep");

        aggressive = ConfigValues.defaultConfig.getBoolean("Allow aggressive EliteMobs");

        passive = ConfigValues.defaultConfig.getBoolean("Allow Passive EliteMobs");

    }

    private static Boolean aggressiveBooleanConfigGrabber(String string) {

        return ConfigValues.defaultConfig.getBoolean(aggressiveConfigNodeCompleter(string));

    }

    private static Boolean passiveBooleanConfigGrabber(String string) {

        return ConfigValues.defaultConfig.getBoolean(passiveConfigNodeCompleter(string));

    }

    private static String aggressiveConfigNodeCompleter(String string) {

        return "Valid aggressive EliteMobs." + string;

    }

    private static String passiveConfigNodeCompleter(String string) {

        return "Valid passive EliteMobs." + string;

    }

    private static void populateValidMobs(Inventory inventory) {

        configStatusChecker();

        /*there are 5 passive mobs and 13 aggressive mobs
        slots 13, 22, 31, 40 and 49 are at the middle of the inventory and will not be used
        left half is occupied by the aggressive mobs, right side is occupied by the passive ones
        */

        List<Integer> aggressiveMobSlots = new ArrayList(Arrays.asList(9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48));
        List<Integer> passiveMobSlots = new ArrayList(Arrays.asList(14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53));

        //populate aggressive mobs
        ItemStack blazeSkull = validMobConfigSkullGenerator("Blaze", blaze);
        inventory.setItem(aggressiveMobSlots.get(0), blazeSkull);

        ItemStack caveSpiderSkull = validMobConfigSkullGenerator("CaveSpider", caveSpider);
        inventory.setItem(aggressiveMobSlots.get(1), caveSpiderSkull);

        ItemStack creeperSkull = validMobConfigSkullGenerator("Creeper", creeper);
        inventory.setItem(aggressiveMobSlots.get(2), creeperSkull);

        ItemStack endermanSkull = validMobConfigSkullGenerator("Enderman", enderman);
        inventory.setItem(aggressiveMobSlots.get(3), endermanSkull);

        ItemStack endermiteSkull = validMobConfigSkullGenerator("Endermite", endermite);
        inventory.setItem(aggressiveMobSlots.get(4), endermiteSkull);

        ItemStack ironGolemSkull = validMobConfigSkullGenerator("IronGolem", ironGolem);
        inventory.setItem(aggressiveMobSlots.get(5), ironGolemSkull);

        ItemStack pigZombieSkull = validMobConfigSkullGenerator("PigZombie", pigZombie);
        inventory.setItem(aggressiveMobSlots.get(6), pigZombieSkull);

        ItemStack polarBearSkull = validMobConfigSkullGenerator("PolarBear", polarBear);
        inventory.setItem(aggressiveMobSlots.get(7), polarBearSkull);

        ItemStack silverfishSkull = validMobConfigSkullGenerator("Silverfish", silverfish);
        inventory.setItem(aggressiveMobSlots.get(8), silverfishSkull);

        ItemStack skeletonSkull = validMobConfigSkullGenerator("Skeleton", skeleton);
        inventory.setItem(aggressiveMobSlots.get(9), skeletonSkull);

        ItemStack spiderSkull = validMobConfigSkullGenerator("Spider", spider);
        inventory.setItem(aggressiveMobSlots.get(10), spiderSkull);

        ItemStack witchSkull = validMobConfigSkullGenerator("Witch", witch);
        inventory.setItem(aggressiveMobSlots.get(11), witchSkull);

        ItemStack zombieSkull = validMobConfigSkullGenerator("Zombie", zombie);
        inventory.setItem(aggressiveMobSlots.get(12), zombieSkull);

        //enable or disable all aggressive mobs
        String aggressiveTitle = "Aggressive Elite Mobs are ";
        aggressiveTitle += aggressive ? "enabled!" : "disabled!";
        List<String> aggressiveLore = new ArrayList<>();
        String aggressiveLoreString = "Click to ";
        aggressiveLoreString += aggressive ? "disable!" : "enable!";
        aggressiveLore.add(aggressiveLoreString);
        ItemStack aggressiveSkull = GUIConfigHandler.skullItemInitializer("MHF_Exclamation", aggressiveTitle, aggressiveLore);
        inventory.setItem(aggressiveMobSlots.get(19), aggressiveSkull);

        //populate passive mobs
        ItemStack chickenSkull = validMobConfigSkullGenerator("Chicken", chicken);
        inventory.setItem(passiveMobSlots.get(0), chickenSkull);

        ItemStack cowSkull = validMobConfigSkullGenerator("Cow", cow);
        inventory.setItem(passiveMobSlots.get(1), cowSkull);

        ItemStack mushroomCowSkull = validMobConfigSkullGenerator("MushroomCow", mushroomCow);
        inventory.setItem(passiveMobSlots.get(2), mushroomCowSkull);

        ItemStack pigSkull = validMobConfigSkullGenerator("Pig", pig);
        inventory.setItem(passiveMobSlots.get(3), pigSkull);

        ItemStack sheepSkull = validMobConfigSkullGenerator("Sheep", sheep);
        inventory.setItem(passiveMobSlots.get(4), sheepSkull);

        //enable or disable all passive mobs
        String passiveTitle = "Passive Elite Mobs are ";
        passiveTitle += passive ? "enabled!" : "disabled!";
        List<String> passiveLore = new ArrayList<>();
        String passiveLoreString = "Click to ";
        passiveLoreString += passive ? "disable!" : "enable!";
        passiveLore.add(passiveLoreString);
        ItemStack passiveSkull = GUIConfigHandler.skullItemInitializer("MHF_Exclamation", passiveTitle, passiveLore);
        inventory.setItem(passiveMobSlots.get(19), passiveSkull);

    }

    private static ItemStack validMobConfigSkullGenerator(String string, Boolean bool) {

        ItemStack itemStack = GUIConfigHandler.skullItemInitializer("MHF_" + string,
                skullTitleGenerator(bool, string), skullLoreGenerator(bool));

        return itemStack;

    }

    private static String skullTitleGenerator(Boolean bool, String string) {

        String finalString = string + " Elite Mobs are ";
        finalString += bool ? "enabled!" : "disabled!";

        return finalString;

    }

    private static List<String> skullLoreGenerator(Boolean bool) {

        String finalString = "Click to ";
        finalString += bool ? "disable!" : "enable!";
        List<String> stringList = new ArrayList<>();
        stringList.add(finalString);

        return stringList;

    }

    private static void repopulateValidMobs(Inventory inventory) {

        GUIConfigHandler.inventoryWiper(inventory);

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = GUIConfigHandler.itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

        populateValidMobs(inventory);

    }

    @EventHandler
    public void validMobsConfigGUIInteraction(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getClickedInventory().getName().equals("Config GUI - Valid Mobs")) {

            event.setCancelled(true);

            if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR)) {

                Player player = (Player) event.getWhoClicked();
                Inventory inventory = event.getInventory();
                ItemStack clickedItem = event.getCurrentItem();
                String name = clickedItem.getItemMeta().getDisplayName();

                if (name.equals("Back To Main Menu")) {

                    player.closeInventory();
                    Inventory smallInventory = GUIConfigHandler.threeRowInventory("Config GUI");
                    player.openInventory(smallInventory);
                    GUIConfigHandler.configPickerPopulator(smallInventory);

                    return;

                }

                if (name.contains("Blaze")) {

                    aggressiveMobConfigSetter("Blaze", inventory);

                    return;

                }

                if (name.contains("CaveSpider")) {

                    aggressiveMobConfigSetter("CaveSpider", inventory);

                    return;

                }

                if (name.contains("Creeper")) {

                    aggressiveMobConfigSetter("Creeper", inventory);

                    return;

                }

                if (name.contains("Enderman")) {

                    aggressiveMobConfigSetter("Enderman", inventory);

                    return;

                }

                if (name.contains("Endermite")) {

                    aggressiveMobConfigSetter("Endermite", inventory);

                    return;

                }

                if (name.contains("IronGolem")) {

                    aggressiveMobConfigSetter("IronGolem", inventory);

                    return;

                }

                if (name.contains("PigZombie")) {

                    aggressiveMobConfigSetter("PigZombie", inventory);

                    return;

                }

                if (name.contains("PolarBear")) {

                    aggressiveMobConfigSetter("PolarBear", inventory);

                    return;

                }

                if (name.contains("Silverfish")) {

                    aggressiveMobConfigSetter("Silverfish", inventory);

                    return;

                }

                if (name.contains("Skeleton")) {

                    aggressiveMobConfigSetter("Skeleton", inventory);

                    return;

                }

                if (name.contains("Spider")) {

                    aggressiveMobConfigSetter("Spider", inventory);

                    return;

                }

                if (name.contains("Witch")) {

                    aggressiveMobConfigSetter("Witch", inventory);

                    return;

                }

                if (name.contains("Zombie")) {

                    aggressiveMobConfigSetter("Zombie", inventory);

                    return;

                }

                if (name.contains("Chicken")) {

                    passiveMobConfigSetter("Chicken", inventory);

                    return;

                }

                if (name.contains("MushroomCow")) {

                    passiveMobConfigSetter("MushroomCow", inventory);

                    return;

                }

                if (name.contains("Cow")) {

                    passiveMobConfigSetter("Cow", inventory);

                    return;

                }

                if (name.contains("Pig")) {

                    passiveMobConfigSetter("Pig", inventory);

                    return;

                }

                if (name.contains("Sheep")) {

                    passiveMobConfigSetter("Sheep", inventory);

                    return;

                }

                if (name.contains("Aggressive")) {

                    ConfigValues.defaultConfig.set("Allow aggressive EliteMobs",
                            !ConfigValues.defaultConfig.getBoolean("Allow aggressive EliteMobs"));

                    DefaultConfig.reloadConfig();
                    repopulateValidMobs(inventory);

                    return;

                }

                if (name.contains("Passive")) {

                    ConfigValues.defaultConfig.set("Allow Passive EliteMobs",
                            !ConfigValues.defaultConfig.getBoolean("Allow Passive EliteMobs"));

                    DefaultConfig.reloadConfig();
                    repopulateValidMobs(inventory);

                    return;

                }

            }

        }

    }

    private void aggressiveMobConfigSetter(String string, Inventory inventory) {

        ConfigValues.defaultConfig.set("Valid aggressive EliteMobs." + string,
                !ConfigValues.defaultConfig.getBoolean("Valid aggressive EliteMobs." + string));

        DefaultConfig.reloadConfig();
        repopulateValidMobs(inventory);

    }

    private void passiveMobConfigSetter(String string, Inventory inventory) {

        ConfigValues.defaultConfig.set("Valid passive EliteMobs." + string,
                !ConfigValues.defaultConfig.getBoolean("Valid passive EliteMobs." + string));

        DefaultConfig.reloadConfig();
        repopulateValidMobs(inventory);

    }

}
