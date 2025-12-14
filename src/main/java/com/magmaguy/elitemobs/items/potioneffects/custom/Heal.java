package com.magmaguy.elitemobs.items.potioneffects.custom;

import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Heal {

    private static final Set<UUID> healCooldown = new HashSet<>();

    public static void shutdown() {
        healCooldown.clear();
    }

    public static void doHeal(Player player, ElitePotionEffect elitePotionEffect) {
        if (healCooldown.contains(player.getUniqueId())) return;
        PlayerPotionEffects.addOnHitCooldown(healCooldown, player, 20 * 5);
        double healedAmount = (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
        player.setHealth(Math.min(player.getHealth() + healedAmount, AttributeManager.getAttributeValue(player, "generic_max_health")));
    }

}
