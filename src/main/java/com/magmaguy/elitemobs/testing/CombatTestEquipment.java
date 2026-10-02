package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

/** Owns temporary diagnostic equipment and restores the exact original contents. */
final class CombatTestEquipment {

    private PlayerInventory inventory;
    private final java.util.UUID playerId;
    private final java.util.Map<Integer, ItemStack> originals = new java.util.LinkedHashMap<>();
    private final java.util.Set<Integer> borrowed = new java.util.LinkedHashSet<>();
    private final String owner = java.util.UUID.randomUUID().toString();
    private static final org.bukkit.NamespacedKey OWNER = new org.bukkit.NamespacedKey("elitemobs", "diagnostic_equipment");
    private int heldSlot;
    private boolean captured;

    CombatTestEquipment(Player player) {
        inventory = player.getInventory();
        playerId = player.getUniqueId();
    }

    void capture() {
        if (captured) return;
        heldSlot = inventory.getHeldItemSlot();
        originals.put(heldSlot, cloneItem(inventory.getItem(heldSlot)));
        for (int slot = 36; slot <= 40; slot++) originals.put(slot, cloneItem(inventory.getItem(slot)));
        captured = true;
        // Borrow copies, so native durability mutations cannot touch the saved real items.
        for (var entry : originals.entrySet()) write(entry.getKey(), cloneItem(entry.getValue()));
    }

    void restore() {
        if (!captured) return;
        Player currentPlayer = org.bukkit.Bukkit.getPlayer(playerId);
        if (currentPlayer != null) inventory = currentPlayer.getInventory();
        RuntimeException failure = null;
        for (int slot : java.util.List.copyOf(borrowed)) {
            try {
                ItemStack current = inventory.getItem(slot);
                ItemStack original = originals.get(slot);
                if (!owned(current) && !java.util.Objects.equals(current, original))
                    throw new IllegalStateException("Diagnostic slot " + slot + " changed externally; original item retained");
                inventory.setItem(slot, cloneItem(original));
                borrowed.remove(slot);
            } catch (RuntimeException error) {
                if (failure == null) failure = error; else failure.addSuppressed(error);
            }
        }
        if (failure != null) throw failure;
        captured = false;
    }

    private boolean owned(ItemStack item) {
        return item != null && item.hasItemMeta() && owner.equals(item.getItemMeta()
                .getPersistentDataContainer().get(OWNER, org.bukkit.persistence.PersistentDataType.STRING));
    }

    private void write(int slot, ItemStack replacement) {
        ItemStack current = inventory.getItem(slot);
        if (!owned(current) && !java.util.Objects.equals(current, originals.get(slot)))
            throw new IllegalStateException("Diagnostic slot " + slot + " changed externally");
        if (replacement != null && !replacement.getType().isAir()) {
            ItemMeta meta = replacement.getItemMeta();
            meta.getPersistentDataContainer().set(OWNER, org.bukkit.persistence.PersistentDataType.STRING, owner);
            meta.setUnbreakable(true);
            replacement.setItemMeta(meta);
        }
        borrowed.add(slot);
        inventory.setItem(slot, replacement);
    }

    void equipWeapon(SkillType skillType) {
        ItemStack weapon = switch (skillType) {
            case SWORDS -> new ItemStack(Material.NETHERITE_SWORD);
            case AXES -> new ItemStack(Material.NETHERITE_AXE);
            case BOWS -> new ItemStack(Material.BOW);
            case CROSSBOWS -> new ItemStack(Material.CROSSBOW);
            case TRIDENTS -> new ItemStack(Material.TRIDENT);
            case HOES -> new ItemStack(Material.NETHERITE_HOE);
            case MACES -> new ItemStack(Material.MACE);
            case STAVES -> {
                try {
                    yield explicitlyIdentified(new ItemStack(Material.WOODEN_SPEAR), SkillType.STAVES);
                } catch (NoSuchFieldError error) {
                    yield explicitlyIdentified(new ItemStack(Material.STICK), SkillType.STAVES);
                }
            }
            case WANDS -> explicitlyIdentified(new ItemStack(Material.BLAZE_ROD), SkillType.WANDS);
            case SPEARS -> {
                try {
                    yield new ItemStack(Material.IRON_SPEAR);
                } catch (NoSuchFieldError error) {
                    yield new ItemStack(Material.TRIDENT);
                }
            }
            case ARMOR -> null;
        };
        if (weapon != null) {
            inventory.setHeldItemSlot(heldSlot);
            write(heldSlot, weapon);
        }
    }

    private static ItemStack explicitlyIdentified(ItemStack itemStack, SkillType skillType) {
        String id = skillType == SkillType.WANDS
                ? com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI.DEFAULT_WAND_ID
                : com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI.DEFAULT_STAFF_ID;
        if (!com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI.applyWeaponData(itemStack, id))
            throw new IllegalStateException("FMM authored weapon is unavailable: " + id);
        return itemStack;
    }

    void hold(ItemStack item) {
        inventory.setHeldItemSlot(heldSlot);
        write(heldSlot, item);
    }

    void equipArmorSet(int level) {
        write(39, createEliteArmor(Material.IRON_HELMET, level));
        write(38, createEliteArmor(Material.IRON_CHESTPLATE, level));
        write(37, createEliteArmor(Material.IRON_LEGGINGS, level));
        write(36, createEliteArmor(Material.IRON_BOOTS, level));
    }

    private ItemStack createEliteArmor(Material material, int level) {
        ItemStack armorPiece = new ItemStack(material);
        ItemMeta meta = armorPiece.getItemMeta();
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        armorPiece.setItemMeta(meta);
        EliteItemManager.setEliteLevel(armorPiece, level);
        new EliteItemLore(armorPiece, false);
        return armorPiece;
    }

    private static ItemStack cloneItem(ItemStack itemStack) {
        return itemStack == null ? null : itemStack.clone();
    }
}
