package com.magmaguy.elitemobs.items.potioneffects.custom;

import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Saturation {

    private static final Set<UUID> saturationCooldown = new HashSet<>();

    public static void shutdown() {
        saturationCooldown.clear();
    }

    public static void doSaturation(Player player, ElitePotionEffect elitePotionEffect) {
        if (saturationCooldown.contains(player.getUniqueId())) return;
        PlayerPotionEffects.addOnHitCooldown(saturationCooldown, player, 20 * 5);
        double foodRestoredAmount = (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
        foodRestoredAmount = foodRestoredAmount + player.getFoodLevel() > 20 ?
                20 - player.getFoodLevel() : foodRestoredAmount;
        player.setFoodLevel((int) (player.getFoodLevel() + foodRestoredAmount));
    }

}
