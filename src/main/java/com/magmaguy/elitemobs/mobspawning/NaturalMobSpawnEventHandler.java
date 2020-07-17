package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.items.customenchantments.HunterEnchantment;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

/**
 * Created by MagmaGuy on 24/04/2017.
 */
public class NaturalMobSpawnEventHandler implements Listener {

    private static boolean ignoreMob = false;
    private static EliteMobEntity storedEliteMobEntity = null;

    public static void setIgnoreMob(boolean bool, EliteMobEntity eliteMobEntity) {
        ignoreMob = bool;
        storedEliteMobEntity = eliteMobEntity;
    }

    private static boolean getIgnoreMob() {
        return ignoreMob;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawn(CreatureSpawnEvent event) {

        if (getIgnoreMob()) {
            ignoreMob = false;
            storedEliteMobEntity.setLivingEntity(event.getEntity());
            EntityTracker.registerEliteMob(storedEliteMobEntity);
            return;
        }

        if (event.isCancelled()) return;
        /*
        Deal with entities spawned within the plugin
         */
        if (EntityTracker.isEliteMob(event.getEntity())) return;

        if (!MobCombatSettingsConfig.doNaturalMobSpawning)
            return;
        if (!ValidWorldsConfig.fileConfiguration.getBoolean("Valid worlds." + event.getEntity().getWorld().getName()))
            return;
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER) &&
                !MobCombatSettingsConfig.doSpawnersSpawnEliteMobs)
            return;
        if (event.getEntity().getCustomName() != null && DefaultConfig.preventEliteMobConversionOfNamedMobs)
            return;

        if (!(event.getSpawnReason() == NATURAL || event.getSpawnReason() == CUSTOM && !DefaultConfig.doStrictSpawningRules))
            return;
        if (!EliteMobProperties.isValidEliteMobType(event.getEntityType()))
            return;

        LivingEntity livingEntity = event.getEntity();


        double validChance = MobCombatSettingsConfig.aggressiveMobConversionPercentage;

        int huntingGearChanceAdder = HunterEnchantment.getHuntingGearBonus(livingEntity);
        validChance += huntingGearChanceAdder;

        if (ValidWorldsConfig.fileConfiguration.getBoolean("Nightmare mode worlds." + event.getEntity().getWorld().getName()))
            validChance += DefaultConfig.nightmareWorldSpawnBonus;

        if (!(ThreadLocalRandom.current().nextDouble() < validChance))
            return;

        NaturalEliteMobSpawnEventHandler.naturalMobProcessor(livingEntity, event.getSpawnReason());

    }

}
