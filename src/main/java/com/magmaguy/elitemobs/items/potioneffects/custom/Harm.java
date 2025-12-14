package com.magmaguy.elitemobs.items.potioneffects.custom;

import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Harm {

    private static final Set<UUID> harmCooldown = new HashSet<>();

    public static void shutdown() {
        harmCooldown.clear();
    }

    public static void doHarm(Player player, ElitePotionEffect elitePotionEffect) {
        if (harmCooldown.contains(player.getUniqueId())) return;
        PlayerPotionEffects.addOnHitCooldown(harmCooldown, player, 20 * 5);
        double harmAmount = (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
        player.damage(harmAmount);
    }

}
