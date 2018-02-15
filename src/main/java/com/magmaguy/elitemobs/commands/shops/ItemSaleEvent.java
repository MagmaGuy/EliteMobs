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

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.elitedrops.ItemWorthCalculator;
import com.magmaguy.elitemobs.elitedrops.ObfuscatedSignatureLoreData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemSaleEvent implements Listener {

    @EventHandler
    public void playerSellEvent(InventoryClickEvent event) {

        if (!SharedShopElements.sellInventoryNullPointerPreventer(event)) {

            return;

        }

        if (!event.getInventory().getName().equals(ShopHandler.SHOP_NAME) &&
                !event.getInventory().getName().equals(CustomShopHandler.SHOP_NAME)) {

            return;

        }

        if (event.getClickedInventory().getName().equals(ShopHandler.SHOP_NAME) ||
                event.getClickedInventory().getName().equals(CustomShopHandler.SHOP_NAME)) {

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

        double amountDeduced = itemValue * (ConfigValues.economyConfig.getDouble(EconomySettingsConfig.RESALE_VALUE) / 100);

        EconomyHandler.addCurrency(UUIDFilter.guessUUI(player.getName()), amountDeduced);

        if (event.getCurrentItem().getAmount() == 1) {

            event.setCurrentItem(new ItemStack(Material.AIR));

        } else if (event.getCurrentItem().getAmount() > 1) {

            int newAmount = event.getCurrentItem().getAmount() - 1;

            event.getCurrentItem().setAmount(newAmount);

        }

        SharedShopElements.sellMessage(player, itemDisplayName, amountDeduced);

    }

}
