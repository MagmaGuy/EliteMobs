package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.easyminecraftgoals.NMSManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Reversible item-component adapter that lets ordinary food be eaten while the client is shown a
 * full hunger bar.
 *
 * <p>The vanilla client will not start using ordinary food at 20 hunger, so changing only the
 * server-side food level is insufficient. While a player is in [Alpha] Advanced Combat System, food in that
 * player's inventory receives an {@code always_edible} food component and twice its original
 * consumption duration. Original values are stored on the stack and restored whenever it leaves
 * player custody or the mode ends. Native consumption effects and animations remain intact.</p>
 */
final class AdvancedFoodItems {

    private static final String MARKER = "advanced_combat_food";
    private static final String DEFAULT_COMPONENT = "advanced_combat_food_default";
    private static final String NUTRITION = "advanced_combat_food_nutrition";
    private static final String SATURATION = "advanced_combat_food_saturation";
    private static final String ALWAYS_EDIBLE = "advanced_combat_food_always_edible";
    private static final String CONSUMPTION_SECONDS = "advanced_combat_food_consumption_seconds";
    private static final float CONSUMPTION_TIME_MULTIPLIER = 2F;
    private static final Map<Material, FoodSnapshot> VANILLA_FOOD = vanillaFoodProperties();

    private AdvancedFoodItems() {
    }

    static void preparePlayerInventory(Player player) {
        if (!AdvancedCombatRuntime.isActive(player)) {
            restorePlayerInventory(player);
            return;
        }
        adaptInventory(player.getInventory());
    }

    static void restorePlayerInventory(Player player) {
        restoreInventory(player.getInventory());
        restoreItem(player.getItemOnCursor());
    }

    static void restoreInventory(Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (restoreItem(stack)) inventory.setItem(slot, stack);
        }
    }

    static void restoreDroppedItem(Item item) {
        ItemStack stack = item.getItemStack();
        if (restoreItem(stack)) item.setItemStack(stack);
    }

    static boolean restoreItem(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey marker = key(MARKER);
        if (!data.has(marker, PersistentDataType.BYTE)) return false;

        Float originalSeconds = data.get(key(CONSUMPTION_SECONDS), PersistentDataType.FLOAT);
        if (originalSeconds != null && NMSManager.getAdapter() == null) return false;

        boolean defaultComponent = byteValue(data, DEFAULT_COMPONENT) != 0;
        Integer nutrition = data.get(key(NUTRITION), PersistentDataType.INTEGER);
        Float saturation = data.get(key(SATURATION), PersistentDataType.FLOAT);
        boolean alwaysEdible = byteValue(data, ALWAYS_EDIBLE) != 0;

        removeAdapterData(data);
        if (defaultComponent) {
            meta.setFood(null);
        } else if (nutrition != null && saturation != null) {
            FoodComponent food = meta.getFood();
            food.setNutrition(nutrition);
            food.setSaturation(saturation);
            food.setCanAlwaysEat(alwaysEdible);
            meta.setFood(food);
        }
        ItemStack restored = stack.clone();
        restored.setItemMeta(meta);
        if (originalSeconds != null)
            restored = NMSManager.getAdapter().withConsumptionSeconds(restored, originalSeconds);
        stack.setItemMeta(restored.getItemMeta());
        return true;
    }

    private static void adaptInventory(Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (adaptItem(stack)) inventory.setItem(slot, stack);
        }
    }

    private static boolean adaptItem(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        FoodSnapshot food = effectiveFood(stack, meta);
        if (food == null || NMSManager.getAdapter() == null) return false;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (data.has(key(CONSUMPTION_SECONDS), PersistentDataType.FLOAT)) return false;
        float originalSeconds = NMSManager.getAdapter().getConsumptionSeconds(stack);
        if (originalSeconds < 0F) return false;
        float adaptedSeconds = originalSeconds * CONSUMPTION_TIME_MULTIPLIER;
        if (!Float.isFinite(adaptedSeconds)) return false;

        // Keep pre-upgrade food snapshots intact when adding duration to an older adapted stack.
        if (!data.has(key(MARKER), PersistentDataType.BYTE)) {
            data.set(key(MARKER), PersistentDataType.BYTE, (byte) 1);
            data.set(key(DEFAULT_COMPONENT), PersistentDataType.BYTE, meta.hasFood() ? (byte) 0 : (byte) 1);
            data.set(key(NUTRITION), PersistentDataType.INTEGER, food.nutrition());
            data.set(key(SATURATION), PersistentDataType.FLOAT, food.saturation());
            data.set(key(ALWAYS_EDIBLE), PersistentDataType.BYTE, food.alwaysEdible() ? (byte) 1 : (byte) 0);
        }
        data.set(key(CONSUMPTION_SECONDS), PersistentDataType.FLOAT, originalSeconds);
        FoodComponent adapted = meta.getFood();
        adapted.setNutrition(food.nutrition());
        adapted.setSaturation(food.saturation());
        adapted.setCanAlwaysEat(true);
        meta.setFood(adapted);
        ItemStack timed = stack.clone();
        timed.setItemMeta(meta);
        timed = NMSManager.getAdapter().withConsumptionSeconds(timed, adaptedSeconds);
        stack.setItemMeta(timed.getItemMeta());
        return true;
    }

    static double saturationOf(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return 0D;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 0D;
        FoodSnapshot food = effectiveFood(stack, meta);
        return food == null || !Float.isFinite(food.saturation())
                ? 0D
                : Math.max(0D, food.saturation());
    }

    static boolean isFood(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta != null && effectiveFood(stack, meta) != null;
    }

    static Set<Material> vanillaFoodMaterials() {
        return VANILLA_FOOD.keySet();
    }

    static double vanillaSaturation(Material material) {
        FoodSnapshot food = VANILLA_FOOD.get(material);
        return food == null ? 0D : food.saturation();
    }

    private static FoodSnapshot effectiveFood(ItemStack stack, ItemMeta meta) {
        if (!meta.hasFood()) return VANILLA_FOOD.get(stack.getType());
        FoodComponent food = meta.getFood();
        return new FoodSnapshot(food.getNutrition(), food.getSaturation(), food.canAlwaysEat());
    }

    /**
     * Spigot exposes custom food overrides through ItemMeta but not the material prototype. Keep
     * the current vanilla prototype values here so ordinary food works on both Spigot and Paper.
     * Saturation is the number of saturation points restored, not the old saturation modifier.
     */
    private static Map<Material, FoodSnapshot> vanillaFoodProperties() {
        Map<Material, FoodSnapshot> foods = new EnumMap<>(Material.class);
        add(foods, Material.APPLE, 4, 2.4F);
        add(foods, Material.BAKED_POTATO, 5, 6F);
        add(foods, Material.BEEF, 3, 1.8F);
        add(foods, Material.BEETROOT, 1, 1.2F);
        add(foods, Material.BEETROOT_SOUP, 6, 7.2F);
        add(foods, Material.BREAD, 5, 6F);
        add(foods, Material.CARROT, 3, 3.6F);
        add(foods, Material.CHICKEN, 2, 1.2F);
        add(foods, Material.CHORUS_FRUIT, 4, 2.4F, true);
        add(foods, Material.COD, 2, .4F);
        add(foods, Material.COOKED_BEEF, 8, 12.8F);
        add(foods, Material.COOKED_CHICKEN, 6, 7.2F);
        add(foods, Material.COOKED_COD, 5, 6F);
        add(foods, Material.COOKED_MUTTON, 6, 9.6F);
        add(foods, Material.COOKED_PORKCHOP, 8, 12.8F);
        add(foods, Material.COOKED_RABBIT, 5, 6F);
        add(foods, Material.COOKED_SALMON, 6, 9.6F);
        add(foods, Material.COOKIE, 2, .4F);
        add(foods, Material.DRIED_KELP, 1, .6F);
        add(foods, Material.ENCHANTED_GOLDEN_APPLE, 4, 9.6F, true);
        add(foods, Material.GLOW_BERRIES, 2, .4F);
        add(foods, Material.GOLDEN_APPLE, 4, 9.6F, true);
        add(foods, Material.GOLDEN_CARROT, 6, 14.4F);
        add(foods, Material.HONEY_BOTTLE, 6, 1.2F, true);
        add(foods, Material.MELON_SLICE, 2, 1.2F);
        add(foods, Material.MUSHROOM_STEW, 6, 7.2F);
        add(foods, Material.MUTTON, 2, 1.2F);
        add(foods, Material.POISONOUS_POTATO, 2, 1.2F);
        add(foods, Material.PORKCHOP, 3, 1.8F);
        add(foods, Material.POTATO, 1, .6F);
        add(foods, Material.PUFFERFISH, 1, .2F);
        add(foods, Material.PUMPKIN_PIE, 8, 4.8F);
        add(foods, Material.RABBIT, 3, 1.8F);
        add(foods, Material.RABBIT_STEW, 10, 12F);
        add(foods, Material.ROTTEN_FLESH, 4, .8F);
        add(foods, Material.SALMON, 2, .4F);
        add(foods, Material.SPIDER_EYE, 2, 3.2F);
        add(foods, Material.SUSPICIOUS_STEW, 6, 7.2F);
        add(foods, Material.SWEET_BERRIES, 2, .4F);
        add(foods, Material.TROPICAL_FISH, 1, .2F);
        return Map.copyOf(foods);
    }

    private static void add(Map<Material, FoodSnapshot> foods, Material material,
                            int nutrition, float saturation) {
        add(foods, material, nutrition, saturation, false);
    }

    private static void add(Map<Material, FoodSnapshot> foods, Material material,
                            int nutrition, float saturation, boolean alwaysEdible) {
        foods.put(material, new FoodSnapshot(nutrition, saturation, alwaysEdible));
    }

    private static byte byteValue(PersistentDataContainer data, String key) {
        Byte value = data.get(key(key), PersistentDataType.BYTE);
        return value == null ? 0 : value;
    }

    private static void removeAdapterData(PersistentDataContainer data) {
        data.remove(key(MARKER));
        data.remove(key(DEFAULT_COMPONENT));
        data.remove(key(NUTRITION));
        data.remove(key(SATURATION));
        data.remove(key(ALWAYS_EDIBLE));
        data.remove(key(CONSUMPTION_SECONDS));
    }

    private static NamespacedKey key(String value) {
        return new NamespacedKey(MetadataHandler.PLUGIN, value);
    }

    private record FoodSnapshot(int nutrition, float saturation, boolean alwaysEdible) {
    }
}
