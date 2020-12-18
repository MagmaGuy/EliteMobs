package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffect;

public class EliteCreeperExplosionHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEliteCreeperDetonation(ExplosionPrimeEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Creeper && EntityTracker.isEliteMob(event.getEntity())))
            return;

        /*
        This is necessary because the propagate the same duration as they have, which is nearly infinite
         */
        for (PotionEffect potionEffect : ((Creeper) event.getEntity()).getActivePotionEffects())
            ((Creeper) event.getEntity()).removePotionEffect(potionEffect.getType());

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());

        int mobLevel = eliteMobEntity.getLevel() < 1 ? 1 : eliteMobEntity.getLevel();

        float newExplosionRange = (float) (event.getRadius() + Math.ceil(0.01 * mobLevel * event.getRadius() *
                MobCombatSettingsConfig.eliteCreeperExplosionMultiplier));

        if (newExplosionRange > 20)
            newExplosionRange = 20;


        event.setRadius(newExplosionRange);

    }

}
