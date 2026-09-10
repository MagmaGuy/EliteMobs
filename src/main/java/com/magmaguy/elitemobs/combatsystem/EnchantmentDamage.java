package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.PlayerItem;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
import java.util.UUID;

/** EM attribution for explicitly authored secondary damage; the caller owns native application. */
public final class EnchantmentDamage {
    private EnchantmentDamage() { }

    public static void apply(UUID attackId, UUID actorId, Map<String, ItemStack> capturedEquipment, Runnable application) {
        if (MetadataHandler.PLUGIN == null || !MetadataHandler.PLUGIN.isEnabled())
            throw new IllegalStateException("EliteMobs damage owner is unavailable");
        double threatBonus = capturedEquipment.entrySet().stream().mapToDouble(entry -> PlayerItem.readLoudStrikesBonus(
                entry.getValue(), com.magmaguy.magmacore.enchantments.EnchantmentDefinition.Slot.valueOf(entry.getKey()), actorId)).sum();
        // The base attack already resolved critical behavior. Secondary effects do not roll again,
        // gain another native damage multiplier, or turn their fixed authored damage into weapon XP.
        var source = new CombatDamageContext.PlayerDamageSource(attackId, null, false, threatBonus);
        CombatDamageContext.runPlayerToEliteBypass(source, application);
    }
}
