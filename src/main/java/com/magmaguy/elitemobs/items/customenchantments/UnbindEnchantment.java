package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.utils.Developer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UnbindEnchantment extends CustomEnchantment {
    public static String key = "unbind";

    public UnbindEnchantment() {
        super(key, true);
    }

    public static ItemStack unbindItem(ItemStack itemStack) {
        ItemStack newItemStack = itemStack.clone();
        ItemMeta itemMeta = newItemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().remove(SoulbindEnchantment.SOULBIND_KEY);
        itemMeta.getPersistentDataContainer().remove(SoulbindEnchantment.PRESTIGE_KEY);
        newItemStack.setItemMeta(itemMeta);
        new EliteItemLore(newItemStack, false);
        Developer.message("unbound item!");
        return newItemStack;
    }

    /*
    public static class UnbindEvents implements Listener {
        @EventHandler (ignoreCancelled = true)
        public void onItemPlace(PrepareAnvilEvent event) {
            if (event.getInventory().getItem(0) == null ||
                    event.getInventory().getItem(0).getType().equals(Material.AIR) ||
                    event.getInventory().getItem(1) == null ||
                    event.getInventory().getItem(1).getType().equals(Material.AIR))
                return;
            if (ItemTagger.getEnchantment(event.getInventory().getItem(0).getItemMeta(), key) > 0 &&
                    SoulbindEnchantment.itemHasSoulbindEnchantment(event.getInventory().getItem(1).getItemMeta())) {
                event.setResult(unbindItem(event.getInventory().getItem(1)));
                event.getInventory().setRepairCost(39);
            } else if (ItemTagger.getEnchantment(event.getInventory().getItem(1).getItemMeta(), key) > 0 &&
                    SoulbindEnchantment.itemHasSoulbindEnchantment(event.getInventory().getItem(0).getItemMeta())) {
                event.setResult(unbindItem(event.getInventory().getItem(0)));
                event.getInventory().setRepairCost(39);
            }

        }
    }

     */

}
