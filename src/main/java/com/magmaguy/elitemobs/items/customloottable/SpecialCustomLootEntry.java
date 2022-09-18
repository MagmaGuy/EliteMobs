package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.SpecialLoot;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;

public class SpecialCustomLootEntry extends CustomLootEntry implements Serializable {
    @Getter
    private final String rawString;
    private final String configFilename;

    public SpecialCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        this.rawString = rawString;
        this.configFilename = configFilename;
        //todo: Integrate Special Loot in its entirety to this class to make it mesh with how the rest of the loot works
        for (String processedString : rawString.split(":")) {
            String[] strings = processedString.split("=");
            if ("wave".equalsIgnoreCase(strings[0])) {
                try {
                    super.setWave(Integer.parseInt(strings[1]));
                } catch (Exception ex) {
                    errorMessage(rawString, configFilename, "wave");
                }
            }
        }
        entries.add(this);
    }

    private SpecialLoot generateSpecialLoot() {
        return new SpecialLoot(rawString, configFilename);
    }

    public ItemStack generateItemStack(Player player) {
        return generateSpecialLoot().generateItemStack(player);
    }

    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        location.getWorld().dropItem(location, generateSpecialLoot().generateItemStack(player));
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        ItemStack itemStack = generateSpecialLoot().generateItemStack(player);
        player.getInventory().addItem(itemStack);
        player.sendMessage(ItemSettingsConfig.getDirectDropSpecialMessage()
                .replace("$amount", itemStack.getAmount() + "")
                .replace("$name", itemStack.getItemMeta().getDisplayName()));
    }
}
