package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class EliteMobsItemDetector {

    public static boolean isEliteMobsItem(ItemStack itemStack) {
        return ItemTagger.isEliteItem(itemStack);
    }

    /**
     * Returns whether an item meta has the soulbind enchantment
     *
     * @param itemMeta Item meta to be evaluated
     * @return Whether the item has the soulbind meta
     */
    @Nullable
    public static boolean hasSoulbindEnchantment(@NotNull ItemMeta itemMeta) {
        return SoulbindEnchantment.itemHasSoulbindEnchantment(itemMeta);
    }

    /**
     * Gets the {@link Player} to whom the item meta is linked.
     *
     * @param itemMeta Item meta to be evaluated.
     * @return Player to whom the meta is linked. Returns null if no player is linked, or if the player linked is not online.
     */
    @Nullable
    public static Player getSoulboundPlayer(@NotNull ItemMeta itemMeta) {
        return SoulbindEnchantment.getSoulboundPlayer(itemMeta);
    }

}
