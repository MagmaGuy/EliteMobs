package com.magmaguy.elitemobs.items.potioneffects.custom;

import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Saturation {

    private static final HashSet<Player> saturationCooldown = new HashSet<>();

    public static void doSaturation(Player player, ElitePotionEffect elitePotionEffect) {
        if (saturationCooldown.contains(player)) return;
        PlayerPotionEffects.addOnHitCooldown(saturationCooldown, player, 20 * 5);
        double foodRestoredAmount = (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
        foodRestoredAmount = foodRestoredAmount + player.getFoodLevel() > 20 ?
                20 : foodRestoredAmount;
        player.setFoodLevel((int) (player.getFoodLevel() + foodRestoredAmount));
    }

}
