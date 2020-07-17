package com.magmaguy.elitemobs.items.potioneffects.custom;

import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Harm {

    private static final HashSet<Player> harmCooldown = new HashSet<>();

    public static void doHarm(Player player, ElitePotionEffect elitePotionEffect) {
        if (harmCooldown.contains(player)) return;
        PlayerPotionEffects.addOnHitCooldown(harmCooldown, player, 20 * 5);
        double harmAmount = (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
        player.damage(harmAmount);
    }

}
