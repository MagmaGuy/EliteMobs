package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.magmacore.enchantments.EnchantmentItems;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** EM's additive book progression, prepared before charging and pinned to both input stacks. */
public final class UpgradeSystem {
    private UpgradeSystem() { }

    public static Preview preview(ItemStack source, ItemStack book) {
        if (source == null || source.getAmount() < 1 || !ItemTagger.isEliteItem(source))
            throw new IllegalArgumentException("An eligible elite target is required");
        Map<String,Integer> additions = EliteEnchantmentItems.readBook(book);
        var custom = EliteEnchantmentItems.custom(source);
        var natives = new HashMap<>(EliteEnchantmentItems.nativeLevels(source));
        for (var entry : additions.entrySet()) {
            if (entry.getKey().startsWith("minecraft:")) {
                var enchantment = Enchantment.getByKey(NamespacedKey.fromString(entry.getKey()));
                if (enchantment == null) throw new IllegalArgumentException("Unknown native enchantment");
                if (WeaponIdentityResolver.isMagicWeapon(source)
                        && !EnchantmentGenerator.supportedMagicEnchantments().contains(enchantment))
                    throw new IllegalArgumentException("Native enchantment is incompatible with this magic weapon");
                natives.merge(enchantment, entry.getValue(), Math::addExact);
            } else custom.merge(entry.getKey(), entry.getValue(), Math::addExact);
        }
        for (var entry : natives.entrySet()) {
            var config = EnchantmentsConfig.getEnchantment(entry.getKey());
            if (config == null || !config.isEnabled() || entry.getValue() > config.getMaxEnchantmentLevel())
                throw new IllegalArgumentException("Native upgrade exceeds EM's configured limit");
        }
        ItemStack nativeDraft = source.clone();
        nativeDraft.setAmount(1);
        var meta = nativeDraft.getItemMeta();
        EnchantmentGenerator.generateEnchantments(meta, natives);
        ItemTagger.registerEnchantments(meta, natives);
        nativeDraft.setItemMeta(meta);
        var shared = EliteEnchantmentItems.ITEMS.previewCustom(nativeDraft, custom);
        ItemStack rendered = shared.previewItem();
        new EliteItemLore(rendered, false);
        return new Preview(source.clone(), book.clone(), nativeDraft, shared, rendered);
    }

    public static ItemStack upgrade(ItemStack source, ItemStack book) { return preview(source, book).apply(source, book); }

    public static boolean isValidUpgrade(ItemStack source, ItemStack book) {
        try { preview(source, book); return true; }
        catch (RuntimeException invalid) { return false; }
    }

    public static boolean isCompatibleBook(ItemStack source, ItemStack book) {
        return isValidUpgrade(source, book);
    }

    public static final class Preview {
        private final ItemStack source, book, nativeDraft, result;
        private final EnchantmentItems.Preview shared;
        private Preview(ItemStack source, ItemStack book, ItemStack nativeDraft, EnchantmentItems.Preview shared, ItemStack result) {
            this.source = source;
            this.book = book;
            this.nativeDraft = nativeDraft;
            this.shared = shared;
            this.result = result;
        }
        public ItemStack apply(ItemStack currentSource, ItemStack currentBook) {
            if (!source.equals(currentSource) || !book.equals(currentBook))
                throw new ConcurrentModificationException("Enchantment inputs changed after preview");
            shared.apply(nativeDraft); // Recheck the captured provider revisions without reconstructing output.
            return result.clone();
        }
    }
}
