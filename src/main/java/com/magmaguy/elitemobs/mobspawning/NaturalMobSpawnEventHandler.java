package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.items.customenchantments.HunterEnchantment;
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

    public static void setIgnoreMob(boolean bool) {
        ignoreMob = bool;
    }

    private static boolean getIgnoreMob() {
        return ignoreMob;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {

        if (getIgnoreMob()) {
            setIgnoreMob(false);
            return;
        }

        if (event.isCancelled()) return;
        /*
        Deal with entities spawned within the plugin
         */
        if (EntityTracker.isEliteMob(event.getEntity())) return;

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.NATURAL_MOB_SPAWNING))
            return;
        if (!ValidWorldsConfig.getBoolean("Valid worlds." + event.getEntity().getWorld().getName()))
            return;
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER) &&
                !ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.SPAWNERS_SPAWN_ELITE_MOBS))
            return;
        if (event.getEntity().getCustomName() != null && ConfigValues.defaultConfig.getBoolean(DefaultConfig.PREVENT_ELITE_MOB_CONVERSION_OF_NAMED_MOBS))
            return;

        if (!(event.getSpawnReason() == NATURAL || event.getSpawnReason() == CUSTOM && !ConfigValues.defaultConfig.getBoolean(DefaultConfig.STRICT_SPAWNING_RULES)))
            return;
        if (!EliteMobProperties.isValidEliteMobType(event.getEntityType()))
            return;

        LivingEntity livingEntity = event.getEntity();

        int huntingGearChanceAdder = HunterEnchantment.getHuntingGearBonus(livingEntity);

        double validChance = (ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.AGGRESSIVE_MOB_CONVERSION_PERCENTAGE) +
                huntingGearChanceAdder) / 100;

        if (!(ThreadLocalRandom.current().nextDouble() < validChance))
            return;

        NaturalEliteMobSpawnEventHandler.naturalMobProcessor(livingEntity, event.getSpawnReason());

    }

}
