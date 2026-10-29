package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/** EM acquisition roles are resolved through the existing authored item identity. */
public final class ItemConsumables {
    private ItemConsumables() { }

    public enum Type { LUCKY_TICKET, UNBIND }

    /**
     * The scrap EliteMobs used to ship, by tier. Scrap is retired: repairs cost coins, scrap players still hold is
     * exchanged for coins, and loot naming these files pays coins instead.
     */
    private static final List<String> RETIRED_SCRAP_FILES = List.of("elite_scrap_tiny.yml", "elite_scrap_small.yml",
            "elite_scrap_medium.yml", "elite_scrap_large.yml", "elite_scrap_huge.yml");

    public record Definition(Type type) {
        public Definition {
            java.util.Objects.requireNonNull(type, "consumable type");
        }
    }

    public static Definition resolve(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() < 1) return null;
        String id = ItemTagger.getCustomItemId(item);
        if (id == null) return null;
        CustomItem authored = CustomItem.getCustomItem(id);
        if (authored == null) return null;
        var fields = authored.getCustomItemsConfigFields();
        if (!fields.isEnabled() || fields.getMaterial() != item.getType()) return null;
        return fields.getConsumable();
    }

    public static boolean is(ItemStack item, Type type) {
        Definition definition = resolve(item);
        return definition != null && definition.type() == type;
    }

    /** Tier 1-5 of held elite scrap, or 0. Scrap is recognized by its tags; its item files are gone. */
    public static int scrapTier(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return 0;
        int tier = retiredScrapTier(ItemTagger.getCustomItemId(item));
        if (tier > 0) return tier;
        // Scrap made before custom item IDs carries only the filename-as-key tag.
        var container = item.getItemMeta().getPersistentDataContainer();
        for (int index = 0; index < RETIRED_SCRAP_FILES.size(); index++)
            if (container.has(new NamespacedKey(MetadataHandler.PLUGIN, RETIRED_SCRAP_FILES.get(index)), PersistentDataType.STRING))
                return index + 1;
        return 0;
    }

    /** Coins one held scrap item is worth, or 0 when the item is not scrap. */
    public static int scrapCoinValue(ItemStack item) {
        return ItemSettingsConfig.getRetiredScrapCoinValue(scrapTier(item));
    }

    /** Tier 1-5 when a filename names scrap EliteMobs used to ship, or 0. */
    public static int retiredScrapTier(String filename) {
        return filename == null ? 0 : RETIRED_SCRAP_FILES.indexOf(filename) + 1;
    }

    public static ItemStack unbind(ItemStack source) {
        ItemStack result = source.clone();
        var meta = result.getItemMeta();
        if (meta == null) throw new IllegalArgumentException("Cannot unbind an item without metadata");
        meta.getPersistentDataContainer().remove(SoulbindEnchantment.SOULBIND_KEY);
        meta.getPersistentDataContainer().remove(SoulbindEnchantment.PRESTIGE_KEY);
        result.setItemMeta(meta);
        new EliteItemLore(result, false);
        return result;
    }
}
