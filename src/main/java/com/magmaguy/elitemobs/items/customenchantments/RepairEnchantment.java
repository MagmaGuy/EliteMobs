package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RepairEnchantment extends CustomEnchantment {
    public static String key = "repair";

    public RepairEnchantment() {
        super(key, false);
    }

    public static ItemStack generateScrap(ItemStack itemToScrap, Player player, EliteEntity eliteEntity) {
        int tier = EliteItemManager.getRoundedItemLevel(itemToScrap);
        int scrapLevel;
        if (tier < 50) scrapLevel = 1;
        else if (tier < 100) scrapLevel = 2;
        else if (tier < 150) scrapLevel = 3;
        else if (tier < 200) scrapLevel = 4;
        else scrapLevel = 5;
        CustomItem scrapItem = switch (scrapLevel) {
            case 1 -> CustomItem.getCustomItem("elite_scrap_tiny.yml");
            case 2 -> CustomItem.getCustomItem("elite_scrap_small.yml");
            case 3 -> CustomItem.getCustomItem("elite_scrap_medium.yml");
            case 4 -> CustomItem.getCustomItem("elite_scrap_large.yml");
            case 5 -> CustomItem.getCustomItem("elite_scrap_huge.yml");
            default -> CustomItem.getCustomItem("elite_scrap_tiny.yml");
        };
        if (scrapItem == null) {
            Logger.warn("Failed to generate scrap! Was the default elite scrap disabled?");
            return null;
        }
        return scrapItem.generateItemStack(scrapLevel, player, eliteEntity);
    }

    public static boolean isRepairItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return false;
        return ItemTagger.hasEnchantment(itemStack.getItemMeta(), RepairEnchantment.key);
    }

    public static int getRepairLevel(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return 0;
        return ItemTagger.getEnchantment(itemStack.getItemMeta(), key);
    }
}
