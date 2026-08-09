package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

/** Owns temporary diagnostic equipment and restores the exact original contents. */
final class CombatTestEquipment {

    private final PlayerInventory inventory;
    private ItemStack mainHand;
    private ItemStack offHand;
    private ItemStack[] armor;
    private boolean captured;

    CombatTestEquipment(Player player) {
        inventory = player.getInventory();
    }

    void capture() {
        if (captured) return;
        mainHand = cloneItem(inventory.getItemInMainHand());
        offHand = cloneItem(inventory.getItemInOffHand());
        ItemStack[] currentArmor = inventory.getArmorContents();
        armor = new ItemStack[currentArmor.length];
        for (int index = 0; index < currentArmor.length; index++) armor[index] = cloneItem(currentArmor[index]);
        captured = true;
    }

    void restore() {
        if (!captured) return;
        inventory.setItemInMainHand(cloneItem(mainHand));
        inventory.setItemInOffHand(cloneItem(offHand));
        ItemStack[] restoredArmor = new ItemStack[armor.length];
        for (int index = 0; index < armor.length; index++) restoredArmor[index] = cloneItem(armor[index]);
        inventory.setArmorContents(restoredArmor);
        captured = false;
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
            case SPEARS -> {
                try {
                    yield new ItemStack(Material.IRON_SPEAR);
                } catch (NoSuchFieldError error) {
                    yield new ItemStack(Material.TRIDENT);
                }
            }
            case ARMOR -> null;
        };
        if (weapon != null) inventory.setItemInMainHand(weapon);
    }

    void equipArmorSet(int level) {
        inventory.setHelmet(createEliteArmor(Material.IRON_HELMET, level));
        inventory.setChestplate(createEliteArmor(Material.IRON_CHESTPLATE, level));
        inventory.setLeggings(createEliteArmor(Material.IRON_LEGGINGS, level));
        inventory.setBoots(createEliteArmor(Material.IRON_BOOTS, level));
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
