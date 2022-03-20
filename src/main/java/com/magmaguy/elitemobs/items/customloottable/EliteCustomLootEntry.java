package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class EliteCustomLootEntry extends CustomLootEntry implements Serializable {
    @Getter
    private String filename = null;

    public EliteCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        super();
        //old format
        if (!rawString.contains("filename=")) {
            parseLegacyFormat(rawString, configFilename);
        }
        //new format
        else {
            parseNewFormat(rawString, configFilename);
        }
        if (filename == null) return;
        CustomItem customItem = CustomItem.getCustomItem(filename);
        if (customItem == null) {
            errorMessage(rawString, configFilename, "filename");
            return;
        }
        entries.add(this);
    }

    //Format: filename.yml:chance:permission
    private void parseLegacyFormat(String rawString, String configFilename) {
        String[] stringArray = rawString.split(":");
        try {
            filename = stringArray[0];
        } catch (Exception ex) {
            errorMessage(rawString, configFilename, "filename");
            return;
        }

        try {
            super.setChance(Double.parseDouble(stringArray[1]));
        } catch (Exception ex) {
            errorMessage(rawString, configFilename, "chance");
            return;
        }

        if (stringArray.length > 2)
            try {
                super.setPermission(stringArray[2]);
            } catch (Exception ex) {
                errorMessage(rawString, configFilename, "permission");
            }
    }

    //Format: filename=filename.yml:chance=X.Y:amount=X:permission=per.miss.ion
    private void parseNewFormat(String rawString, String configFilename) {
        for (String string : rawString.split(":")) {
            String[] strings = string.split("=");
            switch (strings[0].toLowerCase(Locale.ROOT)) {
                case "filename":
                    try {
                        this.filename = strings[1];
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "filename");
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
                case "permission":
                    try {
                        super.setPermission(strings[1]);
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "permission");
                    }
                    break;
                case "itemlevel":
                    try {
                        super.setItemLevel(Integer.parseInt(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "levl");
                    }
                    break;
                case "wave":
                    try {
                        super.setWave(Integer.parseInt(strings[1]));
                    } catch (Exception ex) {
                        errorMessage(rawString, configFilename, "wave");
                    }
                    break;
                default:
            }
        }
    }

    private CustomItem generateCustomItem() {
        return CustomItem.getCustomItem(filename);
    }

    public ItemStack generateItemStack(int level, Player player) {
        return generateCustomItem().generateItemStack(level, player);
    }

    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        for (int i = 0; i < getAmount(); i++)
            generateCustomItem().dropPlayerLoot(player, itemTier, location);
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        for (int i = 0; i < getAmount(); i++)
            player.getInventory().addItem(generateCustomItem().generateItemStack(itemTier, player));
    }
}
