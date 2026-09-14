package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.PluginState;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.magmacore.util.Logger;
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
    public List<String> getDifficultyIDs() { return difficultyIDs == null ? null : List.copyOf(difficultyIDs); }
    private List<String> difficultyIDs = null;
    private final String configFilename;

    public EliteCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
        super();
        this.configFilename = configFilename;
        //old format
        if (!rawString.contains("filename=")) {
            parseLegacyFormat(rawString, configFilename);
        }
        //new format
        else {
            parseNewFormat(rawString, configFilename);
        }
        if (filename == null) return;
        if (CustomItem.isUnavailableWithoutModels(filename)) return;
        CustomItem customItem = CustomItem.getCustomItem(filename);
        if (customItem == null && MetadataHandler.pluginState != PluginState.INITIALIZING)
            errorMessage(rawString, configFilename, "filename");
        entries.add(this);
    }

    public EliteCustomLootEntry(List<CustomLootEntry> entries, Map<?, ?> configMap, String configFilename) {
        this.configFilename = configFilename;
        for (Map.Entry<?, ?> mapEntry : configMap.entrySet()) {
            String key = (String) mapEntry.getKey();
            switch (key.toLowerCase(Locale.ROOT)) {
                case "filename" -> filename = MapListInterpreter.parseString(key, mapEntry.getValue(), configFilename);
                case "chance" ->
                        super.setChance(MapListInterpreter.parseDouble(key, mapEntry.getValue(), configFilename));
                case "difficultyid" ->
                        difficultyIDs = DifficultyResolver.parseFilter(mapEntry.getValue(), configFilename);
                case "permission" ->
                        super.setPermission(MapListInterpreter.parseString(key, mapEntry.getValue(), configFilename));
                case "amount" -> setAmount(MapListInterpreter.parseInteger(key, mapEntry.getValue(), configFilename));
                default -> Logger.warn("Failed to read custom loot option " + key + " in " + configFilename);
            }
        }
        if (!CustomItem.isUnavailableWithoutModels(filename)) entries.add(this);
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
                case "difficultyid":
                    difficultyIDs = DifficultyResolver.parseFilter(strings.length > 1 ? strings[1] : null, configFilename);
                    break;
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
        if (CustomItem.isUnavailableWithoutModels(filename)) return null;
        CustomItem customItem = generateCustomItem();
        if (customItem == null) {
            Logger.warn("Invalid custom item entry! Entry: " + filename);
            return null;
        }
        return customItem.generateItemStack(level, player, eliteEntity);
    }

    public boolean isScalableItem() {
        CustomItem customItem = generateCustomItem();
        return customItem != null && customItem.getScalability() == CustomItem.Scalability.SCALABLE;
    }

    @Override
    public boolean willDrop(Player player) {
        return matchesDungeonDifficulty(player) && super.willDrop(player);
    }

    private boolean matchesDungeonDifficulty(Player player) {
        if (difficultyIDs == null) return true;
        MatchInstance instance = player == null ? null : PlayerData.getMatchInstance(player);
        return !(instance instanceof DungeonInstance dungeon)
                || dungeon.matchesDifficulty(difficultyIDs, configFilename);
    }

    // Chests deliver personal loot, but retain the entry's dungeon difficulty filter.
    @Override
    public boolean locationDrop(int itemTier, Player player, Location location) {
        return dropPhysical(itemTier, player, location, null, false);
    }

    public boolean locationDropExactLevel(int itemTier, Player player, Location location) {
        return dropPhysical(itemTier, player, location, null, true);
    }

    @Override
    public boolean locationDrop(int itemTier, Player player, Location location, EliteEntity eliteEntity) {
        GroupDelivery group = groupDelivery(itemTier, player, eliteEntity);
        if (group != GroupDelivery.PERSONAL) return group == GroupDelivery.DELIVERED;
        return dropPhysical(itemTier, player, location, eliteEntity, false);
    }

    private boolean dropPhysical(int level, Player player, Location location, EliteEntity source, boolean exactLevel) {
        if (CustomItem.isUnavailableWithoutModels(filename)) return false;
        if (!matchesDungeonDifficulty(player) || getAmount() <= 0) return false;
        CustomItem item = generateCustomItem();
        if (item == null) {
            Logger.warn("Invalid loot entry for " + (source == null ? "treasure chest" : "boss " + source.getName())
                    + "! Entry: " + filename);
            return false;
        }
        boolean delivered = false;
        for (int copy = 0; copy < getAmount(); copy++)
            delivered |= (exactLevel ? item.dropPlayerLootExact(player, level, location, source)
                    : item.dropPlayerLoot(player, level, location, source)) != null;
        return delivered;
    }

    @Override
    public boolean directDrop(int itemTier, Player player) {
        return dropDirect(itemTier, player, null, false);
    }

    public boolean directDropExactLevel(int itemTier, Player player) {
        return dropDirect(itemTier, player, null, true);
    }

    @Override
    public boolean directDrop(int itemTier, Player player, EliteEntity eliteEntity) {
        GroupDelivery group = groupDelivery(itemTier, player, eliteEntity);
        if (group != GroupDelivery.PERSONAL) return group == GroupDelivery.DELIVERED;
        return dropDirect(itemTier, player, eliteEntity, false);
    }

    private boolean dropDirect(int level, Player player, EliteEntity source, boolean exactLevel) {
        if (CustomItem.isUnavailableWithoutModels(filename)) return false;
        if (!matchesDungeonDifficulty(player) || getAmount() <= 0) return false;
        CustomItem item = generateCustomItem();
        if (item == null) {
            Logger.warn("Invalid loot entry for " + (source == null ? "direct drop" : "boss " + source.getName())
                    + "! Entry: " + filename);
            return false;
        }
        String name = null;
        int stored = 0;
        boolean delivered = false;
        for (int copy = 0; copy < getAmount(); copy++) {
            ItemStack stack = exactLevel ? item.generateItemStackExact(level, player, source)
                    : item.generateItemStack(level, player, source);
            if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) continue;
            stored += deliver(player, stack);
            // Overflow is awarded on the ground even when nothing fits in the inventory.
            delivered = true;
            if (name == null && stack.getItemMeta() != null)
                name = stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName()
                        : stack.getType().toString().replace("_", " ");
        }
        if (name != null && stored > 0)
            player.sendMessage(ItemSettingsConfig.getDirectDropCustomLootMessage().replace("$itemName", stored + "x " + name));
        return delivered;
    }

    private static int deliver(Player player, ItemStack item) {
        int amount = item.getAmount();
        var overflow = player.getInventory().addItem(item);
        int dropped = 0;
        for (ItemStack leftover : overflow.values()) {
            dropped += leftover.getAmount();
            var entity = player.getWorld().dropItem(player.getLocation(), leftover);
            entity.setOwner(player.getUniqueId());
        }
        return amount - dropped;
    }

    public boolean isClassLoot() {
        CustomItem item = generateCustomItem();
        return item != null && item.getItemType() == CustomItem.ItemType.CLASS_LOOT;
    }

    /** Non-random eligibility; chance remains a probability applied after class selection. */
    public boolean eligibleForClassLoot(Player player, java.util.function.Predicate<List<String>> difficultyFilter) {
        CustomItem item = generateCustomItem();
        return isClassLoot() && item.getCustomItemsConfigFields().isEnabled()
                && getAmount() > 0 && Double.isFinite(getChance()) && getChance() > 0
                && (getPermission().isEmpty() || player != null && player.hasPermission(getPermission()))
                && (item.getPermission().isEmpty() || player != null && player.hasPermission(item.getPermission()))
                && (difficultyIDs == null || difficultyFilter.test(difficultyIDs));
    }

    public boolean isEquipment() {
        return com.magmaguy.elitemobs.items.LootItemPolicy.isEquipment(generateCustomItem());
    }

    private enum GroupDelivery { PERSONAL, SKIPPED, DELIVERED }

    private GroupDelivery groupDelivery(int itemTier, Player player, EliteEntity eliteEntity) {
        if (CustomItem.isUnavailableWithoutModels(filename)) return GroupDelivery.SKIPPED;
        if (!matchesDungeonDifficulty(player) || getAmount() <= 0) return GroupDelivery.SKIPPED;
        if (difficultyIDs != null) {
            MatchInstance matchInstance = PlayerData.getMatchInstance(player);
            if (matchInstance instanceof DungeonInstance) {
                if (!isEquipment()) return GroupDelivery.PERSONAL;
                return addGroupLoot(CustomItem.limitItemLevel(player, itemTier), eliteEntity)
                        ? GroupDelivery.DELIVERED : GroupDelivery.SKIPPED;
            }
        }

        if (!isEquipment() || !PartyManager.shouldUsePartyLoot(player, eliteEntity)) return GroupDelivery.PERSONAL;
        return addPartyLoot(CustomItem.limitItemLevel(player, itemTier), player, eliteEntity);
    }

    private boolean addGroupLoot(int itemTier, EliteEntity eliteEntity) {
        SharedLootTable sharedLootTable = SharedLootTable.getSharedLootTables().get(eliteEntity);
        boolean delivered = false;
        for (int i = 0; i < getAmount(); i++) {
            CustomItem customItem = generateCustomItem();
            if (customItem == null) {
                Logger.warn("Failed to generate a custom item for the boss " + eliteEntity.getName() + "! The configuration file for one of its loot items is not correctly configured.");
                return delivered;
            }
            ItemStack itemStack = customItem.generateItemStack(itemTier, null, eliteEntity);
            if (itemStack == null) {
                Logger.warn("A custom item for boss " + eliteEntity.getName() + " was null! This item will be skipped.");
                return delivered;
            }
            if (sharedLootTable == null) sharedLootTable = new SharedLootTable(eliteEntity);
            delivered |= sharedLootTable.addLoot(itemStack);
        }
        return delivered;
    }

    private GroupDelivery addPartyLoot(int itemTier, Player contributor, EliteEntity eliteEntity) {
        boolean pooledAnyItem = false;
        for (int i = 0; i < getAmount(); i++) {
            CustomItem customItem = generateCustomItem();
            if (customItem == null) {
                Logger.warn("Failed to generate a custom item for the boss " + eliteEntity.getName()
                        + "! The configuration file for one of its loot items is not correctly configured.");
                return pooledAnyItem ? GroupDelivery.DELIVERED : GroupDelivery.SKIPPED;
            }
            ItemStack itemStack = customItem.generateItemStack(itemTier, null, eliteEntity);
            if (itemStack == null) {
                Logger.warn("A custom item for boss " + eliteEntity.getName()
                        + " was null! This item will be skipped.");
                return pooledAnyItem ? GroupDelivery.DELIVERED : GroupDelivery.SKIPPED;
            }
            if (!SharedLootTable.addPartyLoot(eliteEntity, contributor, itemStack)) {
                // The lockout-aware eligibility check may reject a pool which looked viable before
                // the item was generated. Fall back to the normal personal path only if nothing was
                // already committed to the vote, preventing either item loss or duplication.
                return pooledAnyItem ? GroupDelivery.DELIVERED : GroupDelivery.PERSONAL;
            }
            pooledAnyItem = true;
        }
        return pooledAnyItem ? GroupDelivery.DELIVERED : GroupDelivery.SKIPPED;
    }

    @Override
    public ItemStack previewDrop(int itemTier, Player player) {
        return generateItemStack(itemTier, player, null);
    }

    /** Administrator-selected level, without the previewer's progression cap. Fixed/limited item rules still apply. */
    public ItemStack previewDropAtLevel(int level, Player player) {
        CustomItem item = generateCustomItem();
        return item == null ? null : item.generateItemStackExact(level, player, null);
    }
}
