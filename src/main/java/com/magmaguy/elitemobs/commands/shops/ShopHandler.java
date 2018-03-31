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

package com.magmaguy.elitemobs.commands.shops;

import com.magmaguy.elitemobs.commands.guiconfig.SignatureItem;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.elitedrops.ItemWorthCalculator;
import com.magmaguy.elitemobs.elitedrops.ObfuscatedSignatureLoreData;
import com.magmaguy.elitemobs.elitedrops.ProceduralItemGenerator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class ShopHandler implements Listener {

    public static final String SHOP_NAME = ConfigValues.economyConfig.getString(EconomySettingsConfig.SHOP_NAME);

    public void initializeShop(Player player) {

        Inventory shopInventory = Bukkit.createInventory(player, 54, SHOP_NAME);

        populateShop(shopInventory);

        player.openInventory(shopInventory);

    }

    private void populateShop(Inventory shopInventory) {

        SharedShopElements.shopHeader(shopInventory);
        shopContents(shopInventory);

    }

    private static List<Integer> validSlots = (List<Integer>) ConfigValues.economyConfig.getList(EconomySettingsConfig.SHOP_VALID_SLOTS);

    private void shopContents(Inventory shopInventory) {

        Random random = new Random();

        for (int i : validSlots) {

            ProceduralItemGenerator proceduralItemGenerator = new ProceduralItemGenerator();

            int balancedMax = ConfigValues.economyConfig.getInt(EconomySettingsConfig.HIGHEST_PROCEDURALLY_SIMULATED_LOOT)
                    - ConfigValues.economyConfig.getInt(EconomySettingsConfig.LOWEST_PROCEDURALLY_SIMULATED_LOOT);
            int balancedMin = ConfigValues.economyConfig.getInt(EconomySettingsConfig.LOWEST_PROCEDURALLY_SIMULATED_LOOT);

            int randomLevel = random.nextInt(balancedMax + 1) + balancedMin;

            //adjust to item rank
            int level = randomLevel;

            if (level < 1) {

                level = 0;

            }

            shopInventory.setItem(i, proceduralItemGenerator.randomItemGeneratorCommand(level));

        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!SharedShopElements.inventoryNullPointerPreventer(event)) {

            return;

        }

        if (!event.getInventory().getName().equals(SHOP_NAME)) return;

        //reroll loot button
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(SignatureItem.SIGNATURE_ITEMSTACK.getItemMeta().getDisplayName())) {

            populateShop(event.getInventory());
            event.setCancelled(true);
            return;

        }

        if (!ObfuscatedSignatureLoreData.obfuscatedSignatureDetector(event.getCurrentItem())) {

            event.setCancelled(true);
            return;

        }

        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        String itemDisplayName = itemStack.getItemMeta().getDisplayName();

        double itemValue = ItemWorthCalculator.determineItemWorth(itemStack);

        boolean inventoryHasFreeSlots = false;
        for (ItemStack iteratedStack : player.getInventory().getStorageContents()) {

            if (iteratedStack == null) {

                inventoryHasFreeSlots = true;
                break;

            }

        }

        //These slots are for buying items
        if (event.getClickedInventory().getName().equalsIgnoreCase(SHOP_NAME) && validSlots.contains(event.getSlot())) {

            if (!inventoryHasFreeSlots) {

                player.sendMessage("Your inventory is full! You can't buy items until you get some free space.");
                player.closeInventory();

            } else if (EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) >= itemValue) {
                //player has enough money
                EconomyHandler.subtractCurrency(UUIDFilter.guessUUI(player.getName()), itemValue);
                player.getInventory().addItem(itemStack);
                populateShop(event.getInventory());

                SharedShopElements.buyMessage(player, itemDisplayName, itemValue);

            } else {

                player.closeInventory();
                SharedShopElements.insufficientFundsMessage(player, itemValue);

            }

        }

        event.setCancelled(true);

    }


}
