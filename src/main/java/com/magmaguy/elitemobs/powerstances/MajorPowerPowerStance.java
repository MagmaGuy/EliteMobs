package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.mobpowers.majorpowers.*;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerPowerStance implements Listener {

    public static int trackAmount = 2;
    public static int individualEffectsPerTrack = 2;
    private EliteMobEntity eliteMobEntity;

    public MajorPowerPowerStance(EliteMobEntity eliteMobEntity) {

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS))
            return;
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS)
                && !eliteMobEntity.isNaturalEntity())
            return;

        this.eliteMobEntity = eliteMobEntity;

        if (eliteMobEntity.hasMajorVisualEffect()) return;
        eliteMobEntity.setHasMajorVisualEffect(true);

        if (eliteMobEntity.getMajorPowerCount() < 1)
            return;

        /*
        Obfuscate powers to prevent TPS loss
         */
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.OBFUSCATE_MOB_POWERS))
            if (eliteMobEntity.getHasVisualEffectObfuscated()) {
                Object[][] multiDimensionalTrailTracker = new Object[trackAmount][individualEffectsPerTrack];

                for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
                    ArrayList<Object> localObjects = new ArrayList<>();
                    for (int a = 0; a < multiDimensionalTrailTracker.length; a++)
                        localObjects.addAll(addObfuscatedEffects());
                    for (int j = 0; j < multiDimensionalTrailTracker[0].length; j++)
                        if (localObjects.get(j) != null)
                            multiDimensionalTrailTracker[i][j] = localObjects.get(j);

                }

                VisualItemProcessor visualItemProcessor = new VisualItemProcessor(multiDimensionalTrailTracker,
                        MajorPowerStanceMath.cachedVectors, eliteMobEntity.hasMajorVisualEffect(),
                        MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteMobEntity);

                return;
            }

        Object[][] multiDimensionalTrailTracker = new Object[trackAmount][eliteMobEntity.getMajorPowerCount() * individualEffectsPerTrack];

        for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
            ArrayList<Object> localObjects = new ArrayList<>();
            for (int a = 0; a < multiDimensionalTrailTracker.length; a++)
                localObjects.addAll(addAllEffects());
            for (int j = 0; j < multiDimensionalTrailTracker[0].length; j++)
                if (localObjects.get(j) != null)
                    multiDimensionalTrailTracker[i][j] = localObjects.get(j);
        }

        VisualItemProcessor visualItemProcessor = new VisualItemProcessor(multiDimensionalTrailTracker,
                MajorPowerStanceMath.cachedVectors, eliteMobEntity.hasMajorVisualEffect(),
                MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteMobEntity);

    }

    private ArrayList<Object> addObfuscatedEffects() {
        return new ArrayList<>(Arrays.asList(Particle.END_ROD));
    }

    private ArrayList<Object> addAllEffects() {

        ArrayList<Object> effects = new ArrayList<>();

        for (ElitePower elitePower : eliteMobEntity.getPowers()) {

            if (elitePower instanceof SkeletonPillar)
                effects.add(addEffect(Material.BONE));

            if (elitePower instanceof SkeletonTrackingArrow)
                effects.add(addEffect(Material.TIPPED_ARROW));

            if (elitePower instanceof ZombieBloat)
                effects.add(addEffect(Material.RED_MUSHROOM));

            if (elitePower instanceof ZombieFriends)
                effects.add(addEffect(Material.ZOMBIE_HEAD));

            if (elitePower instanceof ZombieNecronomicon)
                effects.add(addEffect(Material.WRITTEN_BOOK));

            if (elitePower instanceof ZombieParents)
                effects.add(addEffect(Material.ZOMBIE_SPAWN_EGG));

            if (elitePower instanceof ZombieTeamRocket)
                effects.add(addEffect(Material.FIREWORK_ROCKET));

        }

        return effects;

    }

    private Object addEffect(Material material) {

        Item item = eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                new ItemStack(material));
        item.setPickupDelay(Integer.MAX_VALUE);
        if (!VersionChecker.currentVersionIsUnder(1, 11))
            item.setGravity(false);
        item.setInvulnerable(true);
        EntityTracker.registerItemVisualEffects(item);
        return item;

    }

    private Object addEffect(Particle particle) {

        return particle;

    }

}
