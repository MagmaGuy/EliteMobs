package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.utils.DialogArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class HealthDisplay implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        if (!MobCombatSettingsConfig.displayHealthOnHit) return;

        int maxHealth = (int) event.getEliteMobEntity().getMaxHealth();
        int currentHealth = (int) (event.getEliteMobEntity().getHealth() - event.getDamage());

        Vector offset = new Vector(0, event.getEliteMobEntity().getLivingEntity().getEyeHeight() + 0.5, 0);

        DialogArmorStand.createDialogArmorStand(event.getEliteMobEntity().getLivingEntity(), setHealthColor(currentHealth, maxHealth) + "" + currentHealth + "/" + maxHealth, offset);
    }

    /*
    Color progression: Dark green - Green - Red - Dark red
     */
    private static ChatColor setHealthColor(int currentHealth, int maxHealth) {

        double healthPercentage = currentHealth * 100 / maxHealth;

        if (healthPercentage > 75)
            return ChatColor.DARK_GREEN;

        if (healthPercentage > 50)
            return ChatColor.GREEN;

        if (healthPercentage > 25)
            return ChatColor.RED;

        if (healthPercentage > 0)
            return ChatColor.DARK_RED;

        return ChatColor.DARK_RED;

    }

}
