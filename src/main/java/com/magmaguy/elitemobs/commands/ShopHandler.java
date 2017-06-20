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

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.elitedrops.ItemRankHandler;
import com.magmaguy.elitemobs.elitedrops.RandomItemGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class ShopHandler implements Listener {

    public void initializeShop(Player player) {

        Inventory shopInventory = Bukkit.createInventory(player, 54, "EliteMobs Shop");

        populateShop(shopInventory);

        player.openInventory(shopInventory);

    }

    private void populateShop(Inventory shopInventory ) {

        shopHeader(shopInventory);
        shopContents(shopInventory);

    }

    private void shopHeader(Inventory shopInventory) {

        ItemStack signature = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta signatureSkullMeta = (SkullMeta) signature.getItemMeta();
        signatureSkullMeta.setOwner("magmaguy");
        signatureSkullMeta.setDisplayName("EliteMobs by MagmaGuy");
        List<String> signatureList = new ArrayList<>();
        signatureList.add("Click me to reroll shop loot!");
        signatureSkullMeta.setLore(signatureList);
        signature.setItemMeta(signatureSkullMeta);

        shopInventory.setItem(4, signature);

    }

    private void shopContents(Inventory shopInventory) {

        //Anything after 8 is populated
        Random random = new Random();

        for (int i = 9; i < 54; i++) {

            RandomItemGenerator randomItemGenerator = new RandomItemGenerator();

            int balancedMax = ConfigValues.economyConfig.getInt("Procedurally Generated Loot.Highest simulated elite mob level loot")
                    - ConfigValues.economyConfig.getInt("Procedurally Generated Loot.Lowest simulated elite mob level loot");
            int balancedMin = ConfigValues.economyConfig.getInt("Procedurally Generated Loot.Lowest simulated elite mob level loot");

            int randomLevel = random.nextInt(balancedMax + 1) + balancedMin;

            //adjust to item rank
            int level = (int) ((randomLevel * ConfigValues.randomItemsConfig.getDouble("Mob level to item rank multiplier")) + (random.nextInt(6) + 1 - 3));

            if (level < 1) {

                level = 0;

            }

            shopInventory.setItem(i, randomItemGenerator.randomItemGeneratorCommand(level));

        }

    }

    @EventHandler
    public void onClick (InventoryClickEvent event) {

        if (!ConfigValues.economyConfig.getBoolean("Enable economy")) {

            return;

        }

        if (event.getInventory().getName().equalsIgnoreCase("EliteMobs Shop")) {

            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {

                event.setCancelled(true);
                return;

            }

            if (event.getSlot() == 4 && event.getClickedInventory().getName().equalsIgnoreCase("EliteMobs Shop")) {
                //reroll loot button

                populateShop(event.getInventory());
                event.setCancelled(true);
                return;

            }


            if (!event.getCurrentItem().getItemMeta().hasLore() || !event.getCurrentItem().getItemMeta().getLore().
                    get(event.getCurrentItem().getItemMeta().getLore().size() - 1).contains(ConfigValues.economyConfig.
                    getString("Currency name"))) {

                event.setCancelled(true);
                return;

            }

            HumanEntity player = event.getWhoClicked();

            String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();
            if (itemDisplayName.equals("EliteMobs by MagmaGuy")) {

                populateShop(event.getInventory());

            }

            ItemStack itemStack = event.getCurrentItem();
            int enchantments = 0;

            if (!itemStack.getEnchantments().isEmpty()) {

                for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {

                    enchantments += entry.getValue();

                }

            }

            int itemRank = ItemRankHandler.guessItemRank(itemStack.getType(), enchantments);
            double itemValue = itemRank * ConfigValues.economyConfig.getDouble("Tier price progression");

            String currencyName = ConfigValues.economyConfig.getString("Currency name");

            if (8 < event.getSlot() && event.getClickedInventory().getName().equalsIgnoreCase("EliteMobs Shop")) {
                //These slots are for buying items

                if (EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) >= itemValue) {
                    //player has enough money
                    EconomyHandler.subtractCurrency(UUIDFilter.guessUUI(player.getName()), itemValue);
                    player.getInventory().addItem(itemStack);
                    populateShop(event.getInventory());

                    player.sendMessage(ChatColor.GREEN + "You have bought " + itemDisplayName + " for " + ChatColor.DARK_GREEN +  itemValue + " " + ChatColor.GREEN + currencyName);
                    player.sendMessage(ChatColor.GREEN + "You have " + ChatColor.DARK_GREEN + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + " " + ChatColor.GREEN + currencyName);

                } else {

                    player.closeInventory();
                    player.sendMessage(ChatColor.RED +"You don't have enough " + currencyName + "!");
                    player.sendMessage(ChatColor.GREEN + "You have " + ChatColor.DARK_GREEN + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + " " + ChatColor.GREEN + currencyName);
                    player.sendMessage(ChatColor.GREEN + "That item cost " + ChatColor.DARK_GREEN + itemValue + " " + ChatColor.GREEN + currencyName + ".");

                }

            }

            if (!event.getClickedInventory().getName().equalsIgnoreCase("EliteMobs Shop")) {
                //These slots are for selling items

                double amountDeduced = itemValue * (ConfigValues.economyConfig.getDouble("Item resale value (percentage)") / 100);

                EconomyHandler.addCurrency(UUIDFilter.guessUUI(player.getName()), amountDeduced);

                event.setCurrentItem(new ItemStack(Material.AIR));

                player.sendMessage("You have sold " + itemDisplayName + " for " + amountDeduced + " " + currencyName);
                player.sendMessage("You have " + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + " " + currencyName);


            }

            event.setCancelled(true);

        }

    }

}
