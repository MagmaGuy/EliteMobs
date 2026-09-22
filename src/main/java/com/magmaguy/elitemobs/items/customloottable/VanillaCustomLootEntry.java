package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VanillaCustomLootEntry extends CustomLootEntry implements Serializable {
    private static final long serialVersionUID = 849502628548667093L;

    @Getter
    private Material material = null;

    public VanillaCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        parse(fields(rawString));
        entries.add(this);
    }

    public VanillaCustomLootEntry(List<CustomLootEntry> entries, Map<String, Object> configMap, String configFilename) {
        parse(fields(configMap));
        entries.add(this);
    }

    private void parse(Map<String, Object> definition) {
        commonFields(definition, false, "material", "type");
        if (definition.containsKey("material") && definition.containsKey("type"))
            throw new IllegalArgumentException("material and type specify the same field");
        String value = text(definition, definition.containsKey("material") ? "material" : "type");
        try {
            material = Material.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException failure) {
            throw new IllegalArgumentException("invalid material " + value, failure);
        }
    }

    public ItemStack generateItemStack() {
        return new ItemStack(material, 1);
    }

    @Override
    public boolean locationDrop(int itemTier, Player player, Location location) {
        if (material == null || material.isAir() || getAmount() <= 0) return false;
        for (int i = 0; i < getAmount(); i++)
            location.getWorld().dropItem(location, generateItemStack());
        return true;
    }

    @Override
    public boolean locationDrop(int itemTier, Player player, Location location, EliteEntity eliteEntity) {
        return locationDrop(itemTier, player, location);
    }

    @Override
    public boolean directDrop(int itemTier, Player player) {
        if (material == null || material.isAir() || getAmount() <= 0) return false;
        String name = null;
        for (int i = 0; i < getAmount(); i++) {
            ItemStack itemStack = generateItemStack();
            var overflow = player.getInventory().addItem(itemStack);
            overflow.values().forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
            if (name == null && itemStack.getItemMeta() != null) {
                if (itemStack.getItemMeta().hasDisplayName()) name = itemStack.getItemMeta().getDisplayName();
                else name = itemStack.getType().toString().replace("_", " ");
            }
        }
        if (name != null)
            player.sendMessage(ItemSettingsConfig.getDirectDropMinecraftLootMessage().replace("$itemName", getAmount() + "x " + name));
        return true;
    }

    @Override
    public ItemStack previewDrop(int itemTier, Player player) {
        return generateItemStack();
    }
}
