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

package com.magmaguy.elitemobs.commands.guiconfig;

import com.magmaguy.elitemobs.commands.guiconfig.configurers.ValidMobsConfigurer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MagmaGuy on 21/10/2017.
 */
public class GUIConfigHandler implements Listener {

    private static ItemStack signature = SignatureItem.signatureItem();

    public void GUIConfigHandler(Player player) {

        //this is the first method called, every other method is summoned by clicking options in it
        Inventory introInventory = threeRowInventory("Config GUI");

        List<String> cautionLore = new ArrayList<>(Arrays.asList("Please read the wiki on GitHub.",
                "You can get support on the discord channel.", "Support plugins you enjoy!"));
        ItemStack caution = itemInitializer(Material.PAPER, "README", cautionLore);
        introInventory.setItem(20, caution);

        List<String> configSelectLore = new ArrayList<>(Arrays.asList("Start configuring EliteMobs!"));
        ItemStack configSelect = itemInitializer(Material.BOOK_AND_QUILL, "Start selecting configs!", configSelectLore);
        introInventory.setItem(24, configSelect);

        player.openInventory(introInventory);

    }

    public static void configPickerPopulator(Inventory inventory) {

        List<String> mobConfigLore = new ArrayList<>(Arrays.asList("Configure Mobs"));
        ItemStack mobConfig = skullItemInitializer(Material.SKULL_ITEM, "MHF_Creeper", "Configure Mobs", mobConfigLore);
        inventory.setItem(19, mobConfig);

        List<String> itemConfigLore = new ArrayList<>(Arrays.asList("Configure Items"));
        ItemStack itemConfig = itemInitializer(Material.DIAMOND_SWORD, "Configure Items", itemConfigLore);
        inventory.setItem(22, itemConfig);

        List<String> miscellanousConfigLore = new ArrayList<>(Arrays.asList("Configure Miscellaneous"));
        ItemStack miscellanousConfig = itemInitializer(Material.BOOKSHELF, "Configure Miscellaneous Settings", miscellanousConfigLore);
        inventory.setItem(25, miscellanousConfig);

    }

    private void mobPickerPopulator(Inventory inventory) {

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

        List<String> mechanicsLore = new ArrayList<>(Arrays.asList("Configure Mob Mechanics"));
        ItemStack mechanics = itemInitializer(Material.PISTON_STICKY_BASE, "Configure Elite Mob Mechanics", mechanicsLore);
        inventory.setItem(19, mechanics);

        List<String> powersMeta = new ArrayList<>(Arrays.asList("Configure Powers"));
        ItemStack powers = itemInitializer(Material.REDSTONE_TORCH_ON, "Configure Elite Mobs Powers", powersMeta);
        inventory.setItem(22, powers);

        List<String> visualEffectMeta = new ArrayList<>(Arrays.asList("Configure Visual Effects", "AKA Power Stances"));
        ItemStack visualEffects = itemInitializer(Material.RED_ROSE, "Configure Visual Effects", visualEffectMeta);
        inventory.setItem(25, visualEffects);

    }

    private void itemPickerPopulator(Inventory inventory) {

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

        List<String> mechanicsLore = new ArrayList<>(Arrays.asList("Configure Item Mechanics"));
        ItemStack mechanics = itemInitializer(Material.PISTON_STICKY_BASE, "Configure Item Mechanics", mechanicsLore);
        inventory.setItem(19, mechanics);

        List<String> customItemMeta = new ArrayList<>(Arrays.asList("Configure Custom Items"));
        ItemStack customItem = itemInitializer(Material.STICK, "Configure Custom Items", customItemMeta);
        inventory.setItem(22, customItem);

        List<String> proceduralLootMeta = new ArrayList<>(Arrays.asList("Configure Procedural Loot", "AKA Power Stances"));
        ItemStack proceduralLoot = itemInitializer(Material.WATCH, "Configure procedurally generated loot", proceduralLootMeta);
        inventory.setItem(25, proceduralLoot);

    }

    private void miscellaneousPickerPopulator(Inventory inventory) {

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

        //todo: branch end

    }

    private void mobMechanicPickerPopulator(Inventory inventory) {

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

        //check if blazes are enabled
        List<String> validMobsLore = new ArrayList<>(Arrays.asList("Valid mobs", "Set which mobs can be elite mobs", "Passive and Aggressive"));
        ItemStack validMobs = skullItemInitializer(Material.SKULL_ITEM, "MHF_Blaze", "Configure Valid Elite Mobs", validMobsLore);
        inventory.setItem(20, validMobs);

    }

    public static Inventory threeRowInventory(String title) {

        Inventory inventory = Bukkit.createInventory(null, 9 * 3, title);

        inventory.setItem(4, signature);

        return inventory;

    }

    public static Inventory sixRowInventory(String title) {

        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);

        inventory.setItem(4, signature);

        return inventory;

    }

    public static ItemStack itemInitializer(Material material, String title, List<String> lore) {

        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(title);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        return item;

    }

    public static ItemStack skullItemInitializer(Material material, String mhfValue, String title, List<String> lore) {

        ItemStack item = new ItemStack(material, 1, (short) 3);
        ItemMeta itemMeta = item.getItemMeta();
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        skullMeta.setOwner(mhfValue);
        item.setItemMeta(skullMeta);
        itemMeta.setDisplayName(title);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        return item;

    }

    private void smallInventoryWiper(Inventory inventory) {

        ItemStack air = new ItemStack(Material.AIR, 1);

        for (int i = 0; i < inventory.getSize(); i++) {

            if (i != 4) {

                inventory.setItem(i, air);

            }

        }

    }

    @EventHandler
    public void configGUIInteraction(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getClickedInventory().getName().equals("Config GUI")) {

            event.setCancelled(true);

            if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR)) {

                Player player = (Player) event.getWhoClicked();
                Inventory inventory = event.getInventory();
                ItemStack clickedItem = event.getCurrentItem();
                String name = clickedItem.getItemMeta().getDisplayName();


                //deal with possibilities in the first menu
                if (name.equals("README")) {

                    player.closeInventory();
                    player.sendRawMessage("-----------------------------------------------------");
                    player.sendRawMessage("I recommend you read the wiki before attempting to change the config. " +
                            "Not every setting might do what you think it will do. You can read the wiki here: " +
                            "https://github.com/MagmaGuy/EliteMobs/wiki Additionally, should you require assistance, you can " +
                            "contact me on my discord room, https://discord.gg/QSA2wgh");
                    player.sendRawMessage("Click me for more details.");
                    player.sendRawMessage("-----------------------------------------------------");

                    return;

                }

                if (name.equals("Start selecting configs!")) {

                    smallInventoryWiper(inventory);
                    configPickerPopulator(inventory);

                    return;

                }

                if (name.equals("Configure Mobs")) {

                    smallInventoryWiper(inventory);
                    mobPickerPopulator(inventory);

                    return;

                }

                if (name.equals("Configure Items")) {

                    smallInventoryWiper(inventory);
                    itemPickerPopulator(inventory);

                    return;

                }

                if (name.equals("Configure Miscellaneous Settings")) {

                    smallInventoryWiper(inventory);
                    miscellaneousPickerPopulator(inventory);

                    return;

                }

                if (name.equals("Back To Main Menu")) {

                    player.closeInventory();
                    Inventory smallInventory = threeRowInventory("Config GUI");
                    player.openInventory(smallInventory);
                    configPickerPopulator(smallInventory);

                    return;

                }

                if (name.equals("Configure Elite Mob Mechanics")) {

                    smallInventoryWiper(inventory);
                    mobMechanicPickerPopulator(inventory);

                    return;

                }

                if (name.equals("Configure Valid Elite Mobs")) {

                    player.closeInventory();
                    Inventory largeInventory = GUIConfigHandler.sixRowInventory("Config GUI - Valid Mobs");
                    player.openInventory(largeInventory);
                    ValidMobsConfigurer.validMobsPickerPopulator(largeInventory);

                    return;

                }

            }

        }

    }

}
