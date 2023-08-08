package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ScalableItemConstructor {

    private ScalableItemConstructor() {
    }

    public static ItemStack randomizeScalableItem(int itemTier, Player player, EliteEntity eliteEntity) {
        CustomItem customItem = CustomItem.getScalableItems().get(ThreadLocalRandom.current().nextInt(CustomItem.getScalableItems().size()));
        return constructScalableItem(itemTier, customItem, player, eliteEntity);
    }

    public static ItemStack constructScalableItem(int itemTier, CustomItem customItem, Player player, EliteEntity eliteEntity) {
        if (!customItem.getPermission().isEmpty() && !player.hasPermission(customItem.getPermission())) return null;
        HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(customItem.getEnchantments());
        return ItemConstructor.constructItem(
                itemTier,
                customItem.getCustomItemsConfigFields().getName(),
                customItem.getCustomItemsConfigFields().getMaterial(),
                newEnchantmentList,
                customItem.getCustomEnchantments(),
                customItem.getPotionEffects(),
                customItem.getCustomItemsConfigFields().getLore(),
                eliteEntity,
                player,
                false,
                customItem.getCustomItemsConfigFields().getCustomModelID(),
                customItem.getCustomItemsConfigFields().isSoulbound()
        );
    }

    private static HashMap<Enchantment, Integer> updateDynamicEnchantments(HashMap<Enchantment, Integer> enchantmentsList) {
        List<Enchantment> enchantmentsArray = new ArrayList<>();
        for (Enchantment enchantment : enchantmentsList.keySet())
            for (int i = 0; i < enchantmentsList.get(enchantment); i++)
                enchantmentsArray.add(enchantment);

        HashMap<Enchantment, Integer> newEnchantmentList = new HashMap<>();

        for (int i = 0; i < enchantmentsArray.size(); i++) {
            int random = ThreadLocalRandom.current().nextInt(0, enchantmentsArray.size());
            if (!newEnchantmentList.containsKey(enchantmentsArray.get(random)))
                newEnchantmentList.put(enchantmentsArray.get(random), 1);
            else {
                int currentValue = newEnchantmentList.get(enchantmentsArray.get(random)) + 1;
                newEnchantmentList.put(enchantmentsArray.get(random), currentValue);
            }
            enchantmentsArray.remove(random);
        }

        return newEnchantmentList;

    }

    /*
    Limited scalable items have the item in the config as the best possible item that the plugin will generate for that
    entry, and every other entry is just a nerfed version of that item. Basically an easy way to limit an item in a
    predictable way.
     */
    public static ItemStack randomizeLimitedItem(int itemTier, Player player, EliteEntity eliteEntity) {

        List<CustomItem> localLootList = new ArrayList<>();

        for (int i = 0; i < itemTier; i++)
            if (CustomItem.getLimitedItems().containsKey(i))
                localLootList.addAll(CustomItem.getLimitedItems().get(i));

            /*
            Currently elitemobs has no way of telling if there will be a limited item available for the specific asked tier
            //todo: fix this
             */
        if (localLootList.size() == 0)
            ItemStackGenerator.generateItemStack(Material.AIR);

        CustomItem customItem = localLootList.get(ThreadLocalRandom.current().nextInt(localLootList.size()));
        if (!customItem.getPermission().isEmpty() && !player.hasPermission(customItem.getPermission())) return null;

        return constructLimitedItem(itemTier, customItem, player, eliteEntity);

    }

    public static ItemStack constructLimitedItem(int itemTier, CustomItem customItem, Player player, EliteEntity eliteEntity) {
        int adjustedItemLevel = Math.min(itemTier, customItem.getItemLevel());

        HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(customItem.getEnchantments());

        return ItemConstructor.constructItem(
                adjustedItemLevel,
                customItem.getCustomItemsConfigFields().getName(),
                customItem.getCustomItemsConfigFields().getMaterial(),
                newEnchantmentList,
                customItem.getCustomEnchantments(),
                customItem.getPotionEffects(),
                customItem.getCustomItemsConfigFields().getLore(),
                eliteEntity,
                player,
                false,
                customItem.getCustomItemsConfigFields().getCustomModelID(),
                customItem.getCustomItemsConfigFields().isSoulbound()
        );

    }

}
