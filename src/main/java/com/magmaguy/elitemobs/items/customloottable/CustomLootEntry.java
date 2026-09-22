package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class CustomLootEntry implements Serializable {
    private static final long serialVersionUID = -9184579043562020517L;

    @Getter
    @Setter
    private double chance = 1;
    @Getter
    @Setter
    private int amount = 1;
    @Getter
    @Setter
    private String permission = "";
    @Getter
    @Setter
    private int wave = -1;
    @Getter
    @Setter
    private int itemLevel = 1;

    public CustomLootEntry() {
    }

    public static void errorMessage(String rawString, String configFilename, String reason) {
        Logger.warn("Failed to parse entry " + rawString + " for file " + configFilename + " due to invalid: " + reason);
    }

    /** Normalize once at definition load; explicit malformed values never inherit defaults. */
    protected static java.util.Map<String, Object> fields(Object raw) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (raw instanceof String value) {
            if (value.regionMatches(true, 0, "minecraft:", 0, 10)) value = value.substring(10);
            for (String token : value.split(":", -1)) {
                String[] pair = token.split("=", 2);
                if (pair.length != 2 || pair[0].isBlank() || pair[1].isBlank())
                    throw new IllegalArgumentException("field " + pair[0] + " requires a value");
                putField(result, pair[0], pair[1]);
            }
        } else if (raw instanceof java.util.Map<?, ?> map) {
            for (var entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key))
                    throw new IllegalArgumentException("loot field names must be strings");
                putField(result, key, entry.getValue());
            }
        } else throw new IllegalArgumentException("loot entry must be a string or map");
        return result;
    }

    private static void putField(java.util.Map<String, Object> fields, String key, Object value) {
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        if (fields.containsKey(normalized)) throw new IllegalArgumentException("duplicate field " + key);
        fields.put(normalized, value);
    }

    protected final void commonFields(java.util.Map<String, Object> fields, boolean supportsPermission,
                                      String... specificFields) {
        java.util.Set<String> allowed = new java.util.HashSet<>(java.util.List.of(
                "amount", "chance", "permission", "wave", "itemlevel"));
        allowed.addAll(java.util.List.of(specificFields));
        for (String key : fields.keySet())
            if (!allowed.contains(key)) throw new IllegalArgumentException("unknown field " + key);
        if (fields.containsKey("amount")) amount = integer(fields, "amount");
        if (fields.containsKey("chance")) {
            Object value = fields.get("chance");
            try {
                chance = Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException failure) {
                throw new IllegalArgumentException("chance must be numeric", failure);
            }
            if (!Double.isFinite(chance)) throw new IllegalArgumentException("chance must be finite");
        }
        if (supportsPermission && fields.containsKey("permission")) permission = text(fields, "permission");
        if (fields.containsKey("wave")) wave = integer(fields, "wave");
        if (fields.containsKey("itemlevel")) itemLevel = integer(fields, "itemlevel");
    }

    protected static int integer(java.util.Map<String, Object> fields, String key) {
        try {
            return Integer.parseInt(String.valueOf(fields.get(key)));
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException(key + " must be an integer", failure);
        }
    }

    protected static String text(java.util.Map<String, Object> fields, String key) {
        if (fields.get(key) instanceof String value && !value.isBlank()) return value;
        throw new IllegalArgumentException(key + " must be a nonempty string");
    }

    public boolean willDrop(Player player) {
        if (amount <= 0 || !Double.isFinite(chance) || chance <= 0) return false;
        if (!permission.isEmpty() && (player == null || !player.hasPermission(permission))) return false;
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    /** Returns whether delivery was accepted; chance is rolled once by the owning loot table. */
    public boolean locationDrop(int itemTier, Player player, Location dropLocation) {
        return false;
    }

    public boolean directDrop(int itemTier, Player player) {
        return false;
    }

    //used specifically so loot can be attributed to the right source
    public boolean locationDrop(int itemTier, Player player, Location dropLocation, EliteEntity eliteEntity) {
        return locationDrop(itemTier, player, dropLocation);
    }

    //used specifically so loot can be attributed to the right source
    public boolean directDrop(int itemTier, Player player, EliteEntity eliteEntity) {
        return directDrop(itemTier, player);
    }

    //Used to preview rewards for quests
    public ItemStack previewDrop(int itemTier, Player player){
        //meant to be overriden by the classes that extend it
        return null;
    }
}
