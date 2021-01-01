package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
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

    public static ItemStack randomizeScalableItem(int itemTier, Player player) {
        CustomItem customItem = CustomItem.getScalableItems().get(ThreadLocalRandom.current().nextInt(CustomItem.getScalableItems().size()));
        return constructScalableItem(itemTier, customItem, player);
    }

    public static ItemStack constructScalableItem(int itemTier, CustomItem customItem, Player player) {
        HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(customItem.getEnchantments(), itemTier, false);
        return ItemConstructor.constructItem(
                customItem.getName(),
                customItem.getMaterial(),
                newEnchantmentList,
                customItem.getCustomEnchantments(),
                customItem.getPotionEffects(),
                customItem.getLore(),
                null,
                player,
                false
        );
    }

    private static HashMap<Enchantment, Integer> updateDynamicEnchantments(HashMap<Enchantment, Integer> enchantmentsList, int itemTier, boolean isLimited) {

        if (enchantmentsList.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL) &&
                enchantmentsList.get(Enchantment.PROTECTION_ENVIRONMENTAL) > ItemSettingsConfig.maximumLootTier)
            itemTier = ItemSettingsConfig.maximumLootTier;

        if (enchantmentsList.containsKey(Enchantment.DAMAGE_ALL) &&
                enchantmentsList.get(Enchantment.DAMAGE_ALL) > ItemSettingsConfig.maximumLootTier)
            itemTier = ItemSettingsConfig.maximumLootTier;

        if (enchantmentsList.containsKey(Enchantment.ARROW_DAMAGE) &&
                enchantmentsList.get(Enchantment.ARROW_DAMAGE) > ItemSettingsConfig.maximumLootTier)
            itemTier = ItemSettingsConfig.maximumLootTier;

        HashMap<Enchantment, Integer> newEnchantmentList = new HashMap<>();
        HashMap<Enchantment, Integer> secondaryEnchantmentList = new HashMap<>();

        /*
        Small limitation of this system, it doesn't take into account the material tier and just defaults to assume it's
        diamond (hence the -1 enchantment).
        It's such a small thing that I don't feel it's worth the hassle of adjusting it.
         */
        for (Enchantment enchantment : enchantmentsList.keySet())
            if (enchantment.equals(Enchantment.DAMAGE_ALL) ||
                    enchantment.equals(Enchantment.ARROW_DAMAGE) ||
                    enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
                newEnchantmentList.put(enchantment, itemTier - 1);
            else
                secondaryEnchantmentList.put(enchantment, enchantmentsList.get(enchantment));

        if (itemTier < 2) return newEnchantmentList;


        int secondaryEnchantmentPool = ThreadLocalRandom.current().nextInt(itemTier);

        for (int i = 0; i < secondaryEnchantmentPool; i++) {

            if (secondaryEnchantmentList.isEmpty()) break;

            int randomIndex = ThreadLocalRandom.current().nextInt(secondaryEnchantmentList.size());
            Enchantment randomizedEnchantment = (Enchantment) secondaryEnchantmentList.keySet().toArray()[randomIndex];

            int enchantmentLevel = 1;

            if (!newEnchantmentList.isEmpty() && newEnchantmentList.containsKey(randomizedEnchantment)) {
                enchantmentLevel = newEnchantmentList.get(randomizedEnchantment) + 1;
            }

            newEnchantmentList.put(randomizedEnchantment, enchantmentLevel);

            if (isLimited) {
                int leftoverPoolEnchantment = secondaryEnchantmentList.get(randomizedEnchantment) - 1;
                if (leftoverPoolEnchantment <= 0)
                    secondaryEnchantmentList.remove(randomizedEnchantment);
                else
                    secondaryEnchantmentList.put(randomizedEnchantment, leftoverPoolEnchantment);
            } else {
                if (EnchantmentsConfig.getEnchantment(randomizedEnchantment).getMaxLevel() == enchantmentLevel)
                    secondaryEnchantmentList.remove(randomizedEnchantment);

            }

        }

        return newEnchantmentList;

    }

    /*
    Limited scalable items have the item in the config as the best possible item that the plugin will generate for that
    entry, and every other entry is just a nerfed version of that item. Basically an easy way to limit an item in a
    predictable way.
     */
    public static ItemStack randomizeLimitedItem(int itemTier, Player player) {

        List<CustomItem> localLootList = new ArrayList<>();

        for (int i = 0; i < itemTier; i++)
            if (CustomItem.getLimitedItem().containsKey(i))
                localLootList.addAll(CustomItem.getLimitedItem().get(i));

            /*
            Currently elitemobs has no way of telling if there will be a limited item available for the specific asked tier
            //todo: fix this
             */
        if (localLootList.size() == 0)
            ItemStackGenerator.generateItemStack(Material.AIR);

        CustomItem customItem = localLootList.get(ThreadLocalRandom.current().nextInt(localLootList.size()));

        return constructLimitedItem(itemTier, customItem, player);

    }

    public static ItemStack constructLimitedItem(int itemTier, CustomItem customItem, Player player) {

        HashMap<Enchantment, Integer> newEnchantmentList = updateLimitedEnchantments(customItem.getEnchantments(), itemTier);

        return ItemConstructor.constructItem(
                customItem.getName(),
                customItem.getMaterial(),
                newEnchantmentList,
                customItem.getCustomEnchantments(),
                customItem.getPotionEffects(),
                customItem.getLore(),
                null,
                player,
                false
        );

    }

    private static HashMap<Enchantment, Integer> updateLimitedEnchantments(HashMap<Enchantment, Integer> enchantmentsList,
                                                                           int itemTier) {
        if (enchantmentsList.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL) &&
                enchantmentsList.get(Enchantment.PROTECTION_ENVIRONMENTAL) < itemTier)
            itemTier = enchantmentsList.get(Enchantment.PROTECTION_ENVIRONMENTAL);

        if (enchantmentsList.containsKey(Enchantment.DAMAGE_ALL) &&
                enchantmentsList.get(Enchantment.DAMAGE_ALL) > ItemSettingsConfig.maximumLootTier)
            itemTier = enchantmentsList.get(Enchantment.DAMAGE_ALL);

        if (enchantmentsList.containsKey(Enchantment.ARROW_DAMAGE) &&
                enchantmentsList.get(Enchantment.ARROW_DAMAGE) > ItemSettingsConfig.maximumLootTier)
            itemTier = enchantmentsList.get(Enchantment.ARROW_DAMAGE);


        return updateDynamicEnchantments(enchantmentsList, itemTier, true);

    }


}
