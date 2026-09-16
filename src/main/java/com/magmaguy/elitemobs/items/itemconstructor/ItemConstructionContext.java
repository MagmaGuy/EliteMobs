package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.freeminecraftmodels.api.ItemConstructionSnapshot;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponKind;
import com.magmaguy.magmacore.enchantments.EnchantmentItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/** One dependency capture for a complete detached item build, passed explicitly through construction. */
public final class ItemConstructionContext {
    private static final NamespacedKey FMM_ITEM_ID = Objects.requireNonNull(NamespacedKey.fromString("freeminecraftmodels:fmm_item_id"));
    private final EnchantmentItems.Construction enchantments;
    private final ItemConstructionSnapshot models;
    private final boolean modelsEnabled;

    private ItemConstructionContext(Collection<CustomItemsConfigFields> configs) {
        Set<String> ids = new HashSet<>(EliteEnchantmentCatalog.definitionIds());
        MagicEnchantmentGeneration.KEYS.forEach(key -> ids.add("freeminecraftmodels:" + key));
        for (var config : configs) for (String entry : config.getEnchantments()) {
            String id = entry.split(",", 2)[0];
            if (!id.startsWith("minecraft:") && id.matches("[a-z0-9._-]+:[a-z0-9/._-]+")) ids.add(id);
        }
        enchantments = EnchantmentItems.prepareConstruction(ids);
        modelsEnabled = Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels");
        models = modelsEnabled ? ItemConstructionSnapshot.capture() : null;
    }

    public static ItemConstructionContext capture(Collection<CustomItemsConfigFields> configs) {
        return new ItemConstructionContext(configs);
    }

    public EnchantmentItems.Construction enchantments() { return enchantments; }
    public boolean modelsEnabled() { return modelsEnabled; }

    public ItemConstructionSnapshot.ItemData model(String id) {
        if (models == null || id == null || id.isBlank()) return null;
        var weapon = models.weapon(id);
        return weapon == null ? models.item(id) : weapon;
    }

    public ItemConstructionSnapshot.ItemData scriptedItem(String id) {
        return models == null ? null : models.item(id);
    }

    public SkillType skill(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        var meta = item.getItemMeta();
        String id = meta == null ? null : meta.getPersistentDataContainer().get(
                FMM_ITEM_ID, PersistentDataType.STRING);
        if (id != null) {
            var data = scriptedItem(id);
            if (data == null || data.authoredId() == null) return null;
            if (data.kind() != null) return data.kind() == MagicWeaponKind.WAND ? SkillType.WANDS : SkillType.STAVES;
        }
        return SkillType.fromMaterial(item.getType());
    }

    public void requireCurrent() {
        enchantments.requireCurrent();
        if (modelsEnabled != Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels"))
            throw new ConcurrentModificationException("FMM availability changed during item construction");
        if (models != null) models.requireCurrent();
    }
}
