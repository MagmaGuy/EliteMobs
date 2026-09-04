package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Canonical item-to-weapon boundary.
 *
 * <p>An EliteMobs identity is authoritative. Material inference is used only when no identity is
 * stored, so a staff carried by {@code WOODEN_SPEAR} can never silently become a spear.</p>
 */
public final class WeaponIdentityResolver {
    public static final NamespacedKey STAFF_ID = NamespacedKey.fromString("elitemobs:staff");
    public static final NamespacedKey WAND_ID = NamespacedKey.fromString("elitemobs:wand");

    private static final String WEAPON_TYPE_KEY_NAME = "weapon_type";
    private static final Map<NamespacedKey, WeaponIdentity> EXPLICIT_IDENTITIES = explicitIdentities();

    private WeaponIdentityResolver() {
    }

    public static WeaponResolution resolve(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) return new WeaponResolution.NotWeapon();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            String storedValue = itemMeta.getPersistentDataContainer().get(key(), PersistentDataType.STRING);
            if (storedValue != null) {
                NamespacedKey parsed = NamespacedKey.fromString(storedValue);
                WeaponIdentity identity = parsed == null ? null : EXPLICIT_IDENTITIES.get(parsed);
                return identity == null
                        ? new WeaponResolution.InvalidExplicit(storedValue, parsed)
                        : new WeaponResolution.Resolved(identity);
            }
        }

        SkillType inferred = SkillType.fromMaterial(itemStack.getType());
        if (inferred == null) return new WeaponResolution.NotWeapon();
        NamespacedKey vanillaId = NamespacedKey.fromString(
                "minecraft:" + inferred.name().toLowerCase(java.util.Locale.ROOT));
        return new WeaponResolution.Resolved(new WeaponIdentity(vanillaId, inferred));
    }

    public static Optional<WeaponIdentity> resolvedIdentity(@Nullable ItemStack itemStack) {
        WeaponResolution resolution = resolve(itemStack);
        return resolution instanceof WeaponResolution.Resolved resolved
                ? Optional.of(resolved.identity())
                : Optional.empty();
    }

    @Nullable
    public static SkillType progressionSkill(@Nullable ItemStack itemStack) {
        return resolvedIdentity(itemStack).map(WeaponIdentity::progressionSkill).orElse(null);
    }

    @Nullable
    public static SkillType progressionSkillIncludingArmor(@Nullable ItemStack itemStack) {
        SkillType weapon = progressionSkill(itemStack);
        if (weapon != null) return weapon;
        if (itemStack == null) return null;
        Material material = itemStack.getType();
        String name = material.name();
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || material == Material.ELYTRA || material == Material.TURTLE_HELMET)
            return SkillType.ARMOR;
        return null;
    }

    public static boolean isMagicWeapon(@Nullable ItemStack itemStack) {
        SkillType skill = progressionSkill(itemStack);
        return skill == SkillType.STAVES || skill == SkillType.WANDS;
    }

    public static void stamp(ItemStack itemStack, SkillType progressionSkill) {
        NamespacedKey weaponId = switch (progressionSkill) {
            case STAVES -> STAFF_ID;
            case WANDS -> WAND_ID;
            default -> throw new IllegalArgumentException(
                    "Only explicitly modeled weapon identities may be stamped: " + progressionSkill);
        };
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) throw new IllegalArgumentException("Item has no metadata");
        itemMeta.getPersistentDataContainer().set(key(), PersistentDataType.STRING, weaponId.toString());
        itemStack.setItemMeta(itemMeta);
    }

    public static NamespacedKey storageKey() {
        return key();
    }

    private static NamespacedKey key() {
        return new NamespacedKey(MetadataHandler.PLUGIN, WEAPON_TYPE_KEY_NAME);
    }

    private static Map<NamespacedKey, WeaponIdentity> explicitIdentities() {
        Map<NamespacedKey, WeaponIdentity> identities = new LinkedHashMap<>();
        identities.put(STAFF_ID, new WeaponIdentity(STAFF_ID, SkillType.STAVES));
        identities.put(WAND_ID, new WeaponIdentity(WAND_ID, SkillType.WANDS));
        return Map.copyOf(identities);
    }
}
