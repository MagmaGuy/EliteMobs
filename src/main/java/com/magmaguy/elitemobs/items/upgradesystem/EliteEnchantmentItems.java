package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.magmacore.enchantments.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.*;

/** Book admission and shared item operations; EM retains its native full-level owner. */
public final class EliteEnchantmentItems {
    private EliteEnchantmentItems() { }
    public static final EnchantmentItems ITEMS = new EnchantmentItems(EnchantmentDefinitions::resolve, EliteEnchantmentItems::profile);

    private static EnchantmentItemProfile profile(ItemStack item) {
        if (WeaponIdentityResolver.resolve(item) instanceof com.magmaguy.elitemobs.skills.WeaponResolution.InvalidExplicit)
            throw new IllegalArgumentException("Authored item provider or definition is unavailable");
        SkillType skill = WeaponIdentityResolver.progressionSkill(item);
        if (skill == SkillType.WANDS) return new EnchantmentItemProfile(EnchantmentDefinition.ItemType.WAND,
                Set.of(EnchantmentDefinition.Slot.MAINHAND), Set.of("WAND_MISSILE"));
        if (skill == SkillType.STAVES) return new EnchantmentItemProfile(EnchantmentDefinition.ItemType.STAFF,
                Set.of(EnchantmentDefinition.Slot.MAINHAND), Set.of("STAFF_FIREBALL", "STAFF_MELEE"));
        return EnchantmentItemProfile.vanilla(item);
    }

    public static Map<String,Integer> custom(ItemStack item) {
        var result = new LinkedHashMap<>(ITEMS.inspect(item));
        result.keySet().removeIf(id -> id.startsWith("minecraft:"));
        return result;
    }

    public static Map<Enchantment,Integer> nativeLevels(ItemStack item) {
        var meta = Objects.requireNonNull(item.getItemMeta(), "item metadata");
        var natives = meta instanceof EnchantmentStorageMeta book ? book.getStoredEnchants() : meta.getEnchants();
        if (natives.keySet().stream().anyMatch(enchantment -> !enchantment.getKey().getNamespace().equals("minecraft")))
            throw new IllegalArgumentException("Unsupported native registry enchantment");
        var result = new LinkedHashMap<Enchantment,Integer>();
        for (Enchantment enchantment : Enchantment.values()) {
            int level = ItemTagger.getEnchantment(meta, enchantment.getKey());
            if (level <= 0 && (natives.containsKey(enchantment)
                    || meta.getPersistentDataContainer().has(enchantment.getKey(), org.bukkit.persistence.PersistentDataType.INTEGER)))
                throw new IllegalArgumentException("Native enchantment levels must be positive");
            if (level > 0) result.put(enchantment, level);
        }
        return result;
    }

    public static Map<String,Integer> readBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK || item.getAmount() < 1)
            throw new IllegalArgumentException("An actual enchanted book is required");
        var result = new LinkedHashMap<>(custom(item));
        // Validate the complete shared record even if another book entry would be usable.
        ITEMS.previewCustom(item, result);
        for (var entry : nativeLevels(item).entrySet()) {
            var config = EnchantmentsConfig.getEnchantment(entry.getKey());
            if (config == null || !config.isEnabled() || entry.getValue() > config.getMaxEnchantmentLevel())
                throw new IllegalArgumentException("Native book enchantment is outside EM's configured policy");
            result.put(entry.getKey().getKey().toString(), entry.getValue());
        }
        if (result.isEmpty()) throw new IllegalArgumentException("Book has no transferable enchantments");
        return Map.copyOf(result);
    }

    public static boolean isEliteEnchantmentBook(ItemStack item) {
        try { return !readBook(item).isEmpty(); }
        catch (IllegalArgumentException | IllegalStateException invalid) { return false; }
    }

    public static boolean isEliteLuckyTicket(ItemStack item) {
        return ItemConsumables.is(item, ItemConsumables.Type.LUCKY_TICKET);
    }
}
