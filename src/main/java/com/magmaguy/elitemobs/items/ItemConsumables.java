package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** EM acquisition roles are resolved through the existing authored item identity. */
public final class ItemConsumables {
    private ItemConsumables() { }

    public enum Type { LUCKY_TICKET, REPAIR_SCRAP, UNBIND }

    public record Definition(Type type, int tier) {
        public Definition {
            java.util.Objects.requireNonNull(type, "consumable type");
            if (type == Type.REPAIR_SCRAP ? tier < 1 || tier > 5 : tier != 0)
                throw new IllegalArgumentException("Repair scrap requires tier 1-5; other consumables have no tier");
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

    public static int repairTier(ItemStack item) {
        Definition definition = resolve(item);
        return definition != null && definition.type() == Type.REPAIR_SCRAP ? definition.tier() : 0;
    }

    public static ItemStack generateScrap(ItemStack source, Player player, EliteEntity elite) {
        int level = EliteItemManager.getRoundedItemLevel(source);
        int tier = level < 50 ? 1 : level < 100 ? 2 : level < 150 ? 3 : level < 200 ? 4 : 5;
        String name = switch (tier) {
            case 1 -> "tiny";
            case 2 -> "small";
            case 3 -> "medium";
            case 4 -> "large";
            default -> "huge";
        };
        CustomItem scrap = CustomItem.getCustomItem("elite_scrap_" + name + ".yml");
        if (scrap == null || !new Definition(Type.REPAIR_SCRAP, tier).equals(scrap.getCustomItemsConfigFields().getConsumable())) {
            Logger.warn("Cannot generate elite scrap: the enabled default item must declare repair_scrap tier " + tier);
            return null;
        }
        return scrap.generateItemStack(tier, player, elite);
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
