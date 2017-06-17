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
import com.magmaguy.elitemobs.elitedrops.ItemRankHandler;
import com.magmaguy.elitemobs.elitedrops.RandomItemGenerator;
import org.bukkit.Bukkit;
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
        RandomItemGenerator randomItemGenerator = new RandomItemGenerator();
        Random random = new Random();

        for (int i = 9; i < 54; i++) {

            //TODO: add config option for limit
            shopInventory.setItem(i, randomItemGenerator.randomItemGeneratorCommand(random.nextInt(150) + 1));

        }

    }

    @EventHandler
    public void onClick (InventoryClickEvent event) {

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


            if (!event.getCurrentItem().getItemMeta().hasLore()) {

                event.setCancelled(true);
                return;

            }

            String[] itemLoreFirstLine = event.getCurrentItem().getItemMeta().getLore().get(0).split(" ");
            String firstWord = itemLoreFirstLine[2] + " " + itemLoreFirstLine[3] + " " + itemLoreFirstLine[4];

            //Don't allow selling items that aren't elite drops (or the head button)
            if (!firstWord.equalsIgnoreCase("Elite Mob Drop") && event.getSlot() != 4) {

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

            event.getWhoClicked().sendMessage("This item is rank " + itemRank);
            event.getWhoClicked().sendMessage("You have " + EconomyHandler.checkCurrency(event.getWhoClicked().getName()));
            player.sendMessage(event.getSlot() + "");

            if (8 < event.getSlot() && event.getClickedInventory().getName().equalsIgnoreCase("EliteMobs Shop")) {
                //These slots are for buying items

                if (EconomyHandler.checkCurrency(player.getName()) >= itemValue) {
                    //player has enough money
                    EconomyHandler.subtractCurrency(player.getName(), itemValue);
                    player.getInventory().addItem(itemStack);
                    populateShop(event.getInventory());

                    player.sendMessage("You have bought " + itemDisplayName + " for " + itemValue + " " + currencyName);
                    player.sendMessage("You have " + EconomyHandler.checkCurrency(player.getName()) + " " + currencyName);

                } else {

                    player.closeInventory();
                    player.sendMessage("You don't have enough " + currencyName + "!");
                    player.sendMessage("You have " + EconomyHandler.checkCurrency(player.getName()) + " " + currencyName);

                }

            }

            if (!event.getClickedInventory().getName().equalsIgnoreCase("EliteMobs Shop")) {
                //These slots are for selling items

                double amountDeduced = itemValue * (ConfigValues.economyConfig.getDouble("Item resale value (percentage)") / 100);

                EconomyHandler.addCurrency(player.getName(), amountDeduced);

                event.setCurrentItem(new ItemStack(Material.AIR));

                player.sendMessage("You have sold " + itemDisplayName + " for " + amountDeduced + " " + currencyName);
                player.sendMessage("You have " + EconomyHandler.checkCurrency(player.getName()) + " " + currencyName);


            }

            event.setCancelled(true);

        }

    }

}
