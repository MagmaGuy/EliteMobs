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
import java.util.concurrent.ThreadLocalRandom;

public class VanillaCustomLootEntry extends CustomLootEntry implements Serializable {
    @Getter
    private Material material = null;

    public VanillaCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        parseAllFormats(rawString, configFilename);
        if (this.material == null) return;
        entries.add(this);
    }

    public VanillaCustomLootEntry(List<CustomLootEntry> entries, Map<String, Object> configMap, String configFilename) {
        parseAllFormats(configMap, configFilename);
        if (this.material == null) return;
        entries.add(this);
    }

    private void parseAllFormats(Map<String, Object> configMap, String configFilename) {
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            String value = entry.getValue().toString();
            switch (key) {
                case "type":
                case "material":
                    try {
                        // Use toUpperCase() to ensure it matches the enum naming conventions
                        this.material = Material.valueOf(value.toUpperCase());
                    } catch (Exception ex) {
                        errorMessage(value, configFilename, "material");
                    }
                    break;
                case "amount":
                    try {
                        super.setAmount(Integer.parseInt(value));
                    } catch (Exception ex) {
                        errorMessage(value, configFilename, "amount");
                    }
                    break;
                case "chance":
                    try {
                        super.setChance(Double.parseDouble(value));
                    } catch (Exception ex) {
                        errorMessage(value, configFilename, "chance");
                    }
                    break;
                case "wave":
                    try {
                        super.setWave(Integer.parseInt(value));
                    } catch (Exception ex) {
                        errorMessage(value, configFilename, "wave");
                    }
                    break;
            }
        }
    }


    private void parseAllFormats(String rawString, String configFilename) {
        for (String processedString : rawString.split(":")) {
            String[] strings = processedString.split("=");
            switch (strings[0].toLowerCase(Locale.ROOT)) {
                case "type":
                case "material":
                    try {
                        this.material = Material.valueOf(strings[1]);
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "material");
                    }
                    break;
                case "amount":
                    try {
                        super.setAmount(Integer.parseInt(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "amount");
                    }
                    break;
                case "chance":
                    try {
                        super.setChance(Double.parseDouble(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "chance");
                    }
                    break;
                case "wave":
                    try {
                        super.setWave(Integer.parseInt(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "wave");
                    }
                    break;
            }
        }
    }

    public ItemStack generateItemStack() {
        return new ItemStack(material, 1);
    }

    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        for (int i = 0; i < getAmount(); i++)
            location.getWorld().dropItem(location, generateItemStack());
    }

    @Override
    public void locationDrop(int itemTier, Player player, Location location, EliteEntity eliteEntity) {
        for (int i = 0; i < getAmount(); i++)
            location.getWorld().dropItem(location, generateItemStack());
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        String name = null;
        for (int i = 0; i < getAmount(); i++) {
            if (ThreadLocalRandom.current().nextDouble() > getChance()) return;
            ItemStack itemStack = generateItemStack();
            player.getInventory().addItem(itemStack);
            if (name == null && itemStack.getItemMeta() != null) {
                if (itemStack.getItemMeta().hasDisplayName()) name = itemStack.getItemMeta().getDisplayName();
                else name = itemStack.getType().toString().replace("_", " ");
            }
        }
        if (name != null)
            player.sendMessage(ItemSettingsConfig.getDirectDropMinecraftLootMessage().replace("$itemName", getAmount() + "x " + name));
    }

    @Override
    public ItemStack previewDrop(int itemTier, Player player) {
        return generateItemStack();
    }
}
