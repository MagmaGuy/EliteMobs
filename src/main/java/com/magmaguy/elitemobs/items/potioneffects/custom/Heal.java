package com.magmaguy.elitemobs.items.potioneffects.custom;

import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.elitemobs.utils.AttributeManager;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Heal {

    private static final HashSet<Player> healCooldown = new HashSet<>();

    public static void doHeal(Player player, ElitePotionEffect elitePotionEffect) {
        if (healCooldown.contains(player)) return;
        PlayerPotionEffects.addOnHitCooldown(healCooldown, player, 20 * 5);
        double healedAmount = (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
        player.setHealth(Math.min(player.getHealth() + healedAmount, AttributeManager.getAttributeBaseValue(player, "generic_max_health")));
    }

}
