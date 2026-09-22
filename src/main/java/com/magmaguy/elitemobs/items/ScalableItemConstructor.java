package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
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
        if (player != null && !customItem.getPermission().isEmpty() && !player.hasPermission(customItem.getPermission())) return null;
        HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(customItem.getEnchantments());
        newEnchantmentList = com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator.withProceduralEnchantments(
                itemTier, customItem.getCustomItemsConfigFields(), newEnchantmentList);
        return ItemConstructor.constructItem(
                itemTier,
                customItem.getCustomItemsConfigFields().getName(),
                customItem.getCustomItemsConfigFields().getMaterial(),
                newEnchantmentList,
                com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator.withProceduralCustomEnchantments(
                        itemTier, customItem.getCustomItemsConfigFields(), customItem.getCustomEnchantments()),
                customItem.getPotionEffects(),
                customItem.getCustomItemsConfigFields().getLore(),
                eliteEntity,
                player,
                false,
                customItem.getCustomItemsConfigFields().getCustomModelID(),
                customItem.getCustomItemsConfigFields().getEquipmentModelID(),
                customItem.getCustomItemsConfigFields().isSoulbound(),
                customItem.getCustomItemsConfigFields().getFilename(),
                customItem.getCustomItemsConfigFields().getScriptedItem(),
                customItem.getCustomItemsConfigFields().getWeaponType(),
                customItem.getCustomItemsConfigFields().getFmmItemModel()
        );
    }

    private static HashMap<Enchantment, Integer> updateDynamicEnchantments(HashMap<Enchantment, Integer> enchantmentsList) {
        return rollEnchantments(enchantmentsList, .5, null, 0);
    }

    /** Shared scalable-loot budget. A reserved primary consumes units from the same draw count. */
    public static HashMap<Enchantment, Integer> rollEnchantments(java.util.Map<Enchantment, Integer> enchantmentsList,
                                                               double fraction, Enchantment primary, int minimumPrimary) {
        if (!Double.isFinite(fraction) || fraction < 0 || fraction > 1)
            throw new IllegalArgumentException("Enchantment budget fraction must be between zero and one");
        List<Enchantment> types = new ArrayList<>();
        List<Integer> capacities = new ArrayList<>();
        long total = 0;
        for (var entry : enchantmentsList.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) continue;
            types.add(entry.getKey());
            capacities.add(count);
            total = Math.addExact(total, count);
        }
        long draws = Math.min(total, (long) Math.ceil(total * fraction));
        int reserved = (int) Math.min(draws, Math.max(0,
                Math.min(minimumPrimary, primary == null ? 0 : enchantmentsList.getOrDefault(primary, 0))));
        int[] remaining = new int[types.size()];
        for (int index = 0; index < remaining.length; index++)
            remaining[index] = capacities.get(index) - (types.get(index).equals(primary) ? reserved : 0);
        long available = total - reserved;
        long needed = draws - reserved;
        // Sampling the omitted units has the same distribution and is cheaper above half budget.
        boolean complement = needed > available / 2;
        long samples = complement ? available - needed : needed;
        int[] picked = new int[types.size()];
        for (long draw = 0; draw < samples; draw++) {
            long selected = ThreadLocalRandom.current().nextLong(available--);
            for (int index = 0; index < remaining.length; index++) {
                if (selected < remaining[index]) {
                    --remaining[index];
                    ++picked[index];
                    break;
                }
                selected -= remaining[index];
            }
        }
        HashMap<Enchantment, Integer> result = new HashMap<>();
        for (int index = 0; index < remaining.length; index++) {
            int count = complement ? remaining[index] : picked[index];
            if (types.get(index).equals(primary)) count += reserved;
            if (count > 0) result.put(types.get(index), count);
        }
        return result;
    }

    /*
    Limited scalable items have the item in the config as the best possible item that the plugin will generate for that
    entry, and every other entry is just a nerfed version of that item. Basically an easy way to limit an item in a
    predictable way.
     */
    public static ItemStack randomizeLimitedItem(int itemTier, Player player, EliteEntity eliteEntity) {

        return randomizeLimitedItem(itemTier, player, eliteEntity, eligibleLimitedItems(itemTier, player));
    }

    public static List<CustomItem> eligibleLimitedItems(int itemTier, Player player) {
        List<CustomItem> eligible = new ArrayList<>();
        for (var bucket : CustomItem.getLimitedItems().entrySet()) {
            // Preserve the authored strict tier boundary; do not scan absent numeric levels.
            if (bucket.getKey() < 0 || bucket.getKey() >= itemTier) continue;
            for (CustomItem item : bucket.getValue())
                if (player == null || LootItemPolicy.canReceive(item, player)) eligible.add(item);
        }
        return eligible;
    }

    public static ItemStack randomizeLimitedItem(int itemTier, Player player, EliteEntity eliteEntity,
                                                List<CustomItem> eligible) {
        if (eligible.isEmpty()) return null;
        return constructLimitedItem(itemTier, eligible.get(ThreadLocalRandom.current().nextInt(eligible.size())),
                player, eliteEntity);
    }

    public static ItemStack constructLimitedItem(int itemTier, CustomItem customItem, Player player, EliteEntity eliteEntity) {
        if (player != null && !LootItemPolicy.canReceive(customItem, player)) return null;
        int adjustedItemLevel = Math.min(itemTier, customItem.getItemLevel());

        HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(customItem.getEnchantments());
        newEnchantmentList = com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator.withProceduralEnchantments(
                adjustedItemLevel, customItem.getCustomItemsConfigFields(), newEnchantmentList);

        return ItemConstructor.constructItem(
                adjustedItemLevel,
                customItem.getCustomItemsConfigFields().getName(),
                customItem.getCustomItemsConfigFields().getMaterial(),
                newEnchantmentList,
                com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator.withProceduralCustomEnchantments(
                        adjustedItemLevel, customItem.getCustomItemsConfigFields(), customItem.getCustomEnchantments()),
                customItem.getPotionEffects(),
                customItem.getCustomItemsConfigFields().getLore(),
                eliteEntity,
                player,
                false,
                customItem.getCustomItemsConfigFields().getCustomModelID(),
                customItem.getCustomItemsConfigFields().getEquipmentModelID(),
                customItem.getCustomItemsConfigFields().isSoulbound(),
                customItem.getCustomItemsConfigFields().getFilename(),
                customItem.getCustomItemsConfigFields().getScriptedItem(),
                customItem.getCustomItemsConfigFields().getWeaponType(),
                customItem.getCustomItemsConfigFields().getFmmItemModel()
        );

    }

}
