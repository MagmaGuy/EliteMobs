package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;

public class ProjectileDamage {

    public static void doGoldNuggetDamage(List<Item> goldNuggets, EliteEntity eliteEntity) {

        new BukkitRunnable() {

            int timer = 0;

            @Override
            public void run() {

                timer++;
                if (goldNuggets.isEmpty()) {
                    cancel();
                    return;
                }

                for (Iterator<Item> iterator = goldNuggets.iterator(); iterator.hasNext(); ) {
                    Item goldNugget = iterator.next();
                    if (goldNugget == null) {
                        iterator.remove();
                        continue;
                    }
                    boolean removed = false;
                    for (Entity entity : goldNugget.getNearbyEntities(0.1, 0.1, 0.1))
                        if (entity != eliteEntity.getLivingEntity() && entity instanceof LivingEntity livingEntity) {
                            if (entity instanceof Player player && player.isBlocking()) continue;
                            BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), livingEntity, 1);
                            removed = true;
                        }

                    if (removed) {
                        iterator.remove();
                        goldNugget.remove();
                    }

                    if (!removed && goldNugget.isOnGround()) {
                        iterator.remove();
                        goldNugget.remove();
                    }
                }

                if (timer < 5 * 20) return;

                for (Iterator<Item> endIterator = goldNuggets.iterator(); endIterator.hasNext(); ) {
                    Item goldNugget = endIterator.next();
                    goldNugget.remove();
                    endIterator.remove();
                }

                cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    public static void configureVisualProjectile(Item goldNugget) {

        EntityTracker.registerVisualEffects(goldNugget);
        goldNugget.setPickupDelay(Integer.MAX_VALUE);

    }

}
