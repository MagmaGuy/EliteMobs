package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.DialogArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class HealthDisplay implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EliteMobDamagedByPlayerEvent event) {

        if (event.isCancelled()) return;

        if (MobCombatSettingsConfig.onlyShowHealthForEliteMobs) {
            if (EntityTracker.isEliteMob(event.getEntity()))
                displayHealth(event.getEliteMobEntity());
            else if (EntityTracker.isSuperMob(event.getEntity()))
                displayHealth(event.getEliteMobEntity());

        } else
            displayHealth(event.getEliteMobEntity());

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EliteMobDamagedByEliteMobEvent event) {

        if (event.isCancelled()) return;

        if (MobCombatSettingsConfig.onlyShowHealthForEliteMobs)
            displayHealth(event.getDamagee());

    }

    public static void displayHealth(EliteMobEntity eliteMobEntity) {

        if (!MobCombatSettingsConfig.displayHealthOnHit) return;

        int maxHealth = (int) eliteMobEntity.getMaxHealth();
        int currentHealth = (int) (eliteMobEntity.getHealth());

        Vector offset = new Vector(0, eliteMobEntity.getLivingEntity().getEyeHeight() + 0.5, 0);

        DialogArmorStand.createDialogArmorStand(eliteMobEntity.getLivingEntity(), setHealthColor(currentHealth, maxHealth) + "" + currentHealth + "/" + maxHealth, offset);

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
