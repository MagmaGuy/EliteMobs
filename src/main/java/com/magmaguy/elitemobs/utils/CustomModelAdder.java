package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;

public class CustomModelAdder {
    public static ItemStack addCustomModel(ItemStack itemStack, String customModelData) {
        if (itemStack == null || itemStack.getItemMeta() == null ||
                customModelData == null || customModelData.isEmpty()) return itemStack;
        itemStack.setItemMeta(addCustomModel(itemStack.getItemMeta(), customModelData));
        return itemStack;
    }

    public static ItemMeta addCustomModel(ItemMeta itemMeta, String customModelData) {
        if (!CustomModelsConfig.useModels) return itemMeta;
        if (!DefaultConfig.useResourcePackModels()) return itemMeta;
        if (VersionChecker.serverVersionOlderThan(21,4) || itemMeta == null) return itemMeta;
        itemMeta.setItemModel(NamespacedKey.fromString(customModelData));
        return itemMeta;
    }

    /**
     * Adds a custom equippable model (asset_id) to armor items for custom armor textures when worn.
     * This sets the equipment model that determines how the armor looks on the player's body.
     * Note: For helmets, we set the slot but NOT the model, so the item_model renders on head instead.
     * @param itemStack The armor item to modify
     * @param equipmentModelId The equipment model ID (e.g., "elitemobs:bronze") - ignored for helmets
     * @return The modified ItemStack
     */
    public static ItemStack addEquippableModel(ItemStack itemStack, String equipmentModelId) {
        if (itemStack == null || itemStack.getItemMeta() == null) return itemStack;
        if (!CustomModelsConfig.useModels) return itemStack;
        if (!DefaultConfig.useResourcePackModels()) return itemStack;
        if (VersionChecker.serverVersionOlderThan(21, 4)) return itemStack;

        EquipmentSlot slot = getArmorSlot(itemStack.getType());
        if (slot == null) return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        EquippableComponent equippable = itemMeta.getEquippable();
        equippable.setSlot(slot);

        // For helmets: don't set equipment model so the item_model renders on head instead
        // For other armor: set the equipment model for custom worn textures
        if (slot != EquipmentSlot.HEAD && equipmentModelId != null && !equipmentModelId.isEmpty()) {
            equippable.setModel(NamespacedKey.fromString(equipmentModelId));
        }

        itemMeta.setEquippable(equippable);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Adds a custom equippable model (asset_id) to armor items for custom armor textures when worn.
     * @param itemMeta The ItemMeta to modify
     * @param equipmentModelId The equipment model ID (e.g., "elitemobs:bronze")
     * @return The modified ItemMeta
     * @deprecated Use {@link #addEquippableModel(ItemStack, String)} instead to ensure slot is set correctly
     */
    @Deprecated
    public static ItemMeta addEquippableModel(ItemMeta itemMeta, String equipmentModelId) {
        if (!CustomModelsConfig.useModels) return itemMeta;
        if (!DefaultConfig.useResourcePackModels()) return itemMeta;
        if (VersionChecker.serverVersionOlderThan(21,4) || itemMeta == null) return itemMeta;
        EquippableComponent equippable = itemMeta.getEquippable();
        equippable.setModel(NamespacedKey.fromString(equipmentModelId));
        itemMeta.setEquippable(equippable);
        return itemMeta;
    }

    /**
     * Determines the equipment slot for a given armor material.
     * @param material The material to check
     * @return The EquipmentSlot or null if not armor
     */
    private static EquipmentSlot getArmorSlot(Material material) {
        String name = material.name();
        if (name.endsWith("_HELMET") || name.equals("CARVED_PUMPKIN") || name.endsWith("_HEAD") || name.endsWith("_SKULL"))
            return EquipmentSlot.HEAD;
        if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA"))
            return EquipmentSlot.CHEST;
        if (name.endsWith("_LEGGINGS"))
            return EquipmentSlot.LEGS;
        if (name.endsWith("_BOOTS"))
            return EquipmentSlot.FEET;
        return null;
    }
}
