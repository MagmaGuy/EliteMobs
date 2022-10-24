package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EliteCustomLootEntry extends CustomLootEntry implements Serializable {
    @Getter
    private String filename = null;
    private String difficultyID = null;
    private String permission = null;

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

    public EliteCustomLootEntry(List<CustomLootEntry> entries, Map<?, ?> configMap, String configFilename) {
        for (Map.Entry<?, ?> mapEntry : configMap.entrySet()) {
            String key = (String) mapEntry.getKey();
            switch (key.toLowerCase()) {
                case "filename" -> filename = MapListInterpreter.parseString(key, mapEntry.getValue(), configFilename);
                case "chance" ->
                        super.setChance(MapListInterpreter.parseDouble(key, mapEntry.getValue(), configFilename));
                case "difficultyid" ->
                        difficultyID = MapListInterpreter.parseString(key, mapEntry.getValue(), configFilename);
                case "permission" ->
                        super.setPermission(MapListInterpreter.parseString(key, mapEntry.getValue(), configFilename));
                default -> new WarningMessage("Failed to read custom loot option " + key + " in " + configFilename);
            }
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
                        errorMessage(rawString, configFilename, "level");
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

    public ItemStack generateItemStack(int level, Player player, EliteEntity eliteEntity) {
        return generateCustomItem().generateItemStack(level, player, eliteEntity);
    }

    @Override
    public void locationDrop(int itemTier, Player player, Location location) {
        for (int i = 0; i < getAmount(); i++)
            generateCustomItem().dropPlayerLoot(player, itemTier, location, null);
    }

    //This is for the boss drop
    @Override
    public void locationDrop(int itemTier, Player player, Location location, EliteEntity eliteEntity) {
        if (isGroupLoot(itemTier, player, eliteEntity)) return;
        for (int i = 0; i < getAmount(); i++)
            generateCustomItem().dropPlayerLoot(player, itemTier, location, eliteEntity);
    }

    @Override
    public void directDrop(int itemTier, Player player) {
        String name = null;
        for (int i = 0; i < getAmount(); i++) {
            ItemStack itemStack = generateCustomItem().generateItemStack(itemTier, player, null);
            player.getInventory().addItem(itemStack);
            if (name == null && itemStack.getItemMeta() != null) {
                if (itemStack.getItemMeta().hasDisplayName()) name = itemStack.getItemMeta().getDisplayName();
                else name = itemStack.getType().toString().replace("_", " ");
            }
        }
        if (name != null)
            player.sendMessage(ItemSettingsConfig.getDirectDropCustomLootMessage().replace("$itemName", getAmount() + "x " + name));
    }

    //This is the drop for boss loot
    @Override
    public void directDrop(int itemTier, Player player, EliteEntity eliteEntity) {
        if (isGroupLoot(itemTier, player, eliteEntity)) return;
        String name = null;
        for (int i = 0; i < getAmount(); i++) {
            ItemStack itemStack = generateCustomItem().generateItemStack(itemTier, player, eliteEntity);
            if (itemStack == null) return;
            player.getInventory().addItem(itemStack);
            if (name == null && itemStack.getItemMeta() != null) {
                if (itemStack.getItemMeta().hasDisplayName()) name = itemStack.getItemMeta().getDisplayName();
                else name = itemStack.getType().toString().replace("_", " ");
            }
        }
        if (name != null)
            player.sendMessage(ItemSettingsConfig.getDirectDropCustomLootMessage().replace("$itemName", getAmount() + "x " + name));
    }

    private boolean isGroupLoot(int itemTier, Player player, EliteEntity eliteEntity) {
        if (difficultyID == null) return false;
        MatchInstance matchInstance = PlayerData.getMatchInstance(player);
        String dungeonDifficultyID = null;
        if (matchInstance instanceof DungeonInstance dungeonInstance)
            dungeonDifficultyID = dungeonInstance.getDifficultyID();
        if (dungeonDifficultyID == null) return false;
        //Beyond this point the item is for instanced dungeons
        if (!dungeonDifficultyID.equals(difficultyID)) return true;
        addGroupLoot(itemTier, eliteEntity);
        return true;
    }

    private void addGroupLoot(int itemTier, EliteEntity eliteEntity) {
        SharedLootTable sharedLootTable = SharedLootTable.getSharedLootTables().get(eliteEntity);
        String name = null;
        for (int i = 0; i < getAmount(); i++) {
            ItemStack itemStack = generateCustomItem().generateItemStack(itemTier, null, eliteEntity);
            if (sharedLootTable == null) sharedLootTable = new SharedLootTable(eliteEntity);
            if (itemStack == null) return;
            sharedLootTable.addLoot(itemStack);
            if (name == null && itemStack.getItemMeta() != null) {
                if (itemStack.getItemMeta().hasDisplayName()) name = itemStack.getItemMeta().getDisplayName();
                else name = itemStack.getType().toString().replace("_", " ");
            }
        }
    }
}
