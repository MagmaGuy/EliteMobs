package com.magmaguy.elitemobs.mobconstructor.displays;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class HealthDisplay implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EliteMobDamagedByPlayerEvent event) {

        if (event.isCancelled()) return;

        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ONLY_SHOW_HEALTH_FOR_ELITE_MOBS)) {
            if (EntityTracker.isEliteMob(event.getEntity()))
                displayHealth(event.getEliteMobEntity());
            else if (EntityTracker.isSuperMob(event.getEntity()))
                displayHealth(event.getEliteMobEntity());

        } else
            displayHealth(event.getEliteMobEntity());

    }

    public static void displayHealth(EliteMobEntity eliteMobEntity) {

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISPLAY_HEALTH_ON_HIT)) return;

        int maxHealth = (int) eliteMobEntity.getMaxHealth();
        int currentHealth = (int) (eliteMobEntity.getHealth());

        Location entityLocation = new Location(eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntity.getLivingEntity().getLocation().getX(),
                eliteMobEntity.getLivingEntity().getLocation().getY() + eliteMobEntity.getLivingEntity().getEyeHeight() + 0.5, eliteMobEntity.getLivingEntity().getLocation().getZ());

        /*
        Dirty fix: armorstands don't render invisibly on their first tick, so it gets moved elsewhere temporarily
         */
        ArmorStand armorStand = (ArmorStand) entityLocation.getWorld().spawnEntity(entityLocation.add(new Vector(0, -50, 0)), EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        armorStand.setMarker(true);
        armorStand.setCustomName(setHealthColor(currentHealth, maxHealth) + "" + currentHealth + "/" + maxHealth);
        armorStand.setGravity(false);
        EntityTracker.registerArmorStands(armorStand);
        armorStand.setCustomNameVisible(false);


        new BukkitRunnable() {

            int taskTimer = 0;

            @Override
            public void run() {

                Location newLocation = new Location(eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntity.getLivingEntity().getLocation().getX(),
                        eliteMobEntity.getLivingEntity().getLocation().getY() + eliteMobEntity.getLivingEntity().getEyeHeight() + 0.5, eliteMobEntity.getLivingEntity().getLocation().getZ());

                armorStand.teleport(newLocation);

                if (taskTimer == 1)
                    armorStand.setCustomNameVisible(true);

                taskTimer++;

                if (taskTimer > 15) {

                    EntityTracker.unregisterArmorStand(armorStand);
                    cancel();

                }

            }

        }.runTaskTimer(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 0, 1L);

    }

    /*
    Color progression: Dark green - Green - Red - Dark red
     */
    private static ChatColor setHealthColor(int currentHealth, int maxHealth) {

        double healthPercentage = currentHealth * 100 / maxHealth;

        if (healthPercentage > 75) {

            return ChatColor.DARK_GREEN;

        }

        if (healthPercentage > 50) {

            return ChatColor.GREEN;

        }

        if (healthPercentage > 25) {

            return ChatColor.RED;

        }

        if (healthPercentage > 0) {

            return ChatColor.DARK_RED;

        }

        return ChatColor.DARK_RED;

    }

}
