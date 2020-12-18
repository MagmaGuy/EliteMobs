package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.MinorPower;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class MinorPowerPowerStance implements Listener {

    public static int trackAmount = 2;
    public static int individualEffectsPerTrack = 2;
    private EliteMobEntity eliteMobEntity;

    //Secondary effect item processing
    public MinorPowerPowerStance(EliteMobEntity eliteMobEntity) {

        if (!MobCombatSettingsConfig.enableVisualEffectsForNaturalMobs)
            return;
        if (MobCombatSettingsConfig.disableVisualEffectsForSpawnerMobs && !eliteMobEntity.isNaturalEntity())
            return;

        this.eliteMobEntity = eliteMobEntity;
        if (eliteMobEntity.hasMinorVisualEffect()) return;
        eliteMobEntity.setHasMinorVisualEffect(true);

        if (eliteMobEntity.getMinorPowerCount() < 1)
            return;

        /*
        Obfuscate powers to prevent TPS loss
         */
        if (MobCombatSettingsConfig.obfuscateMobPowers)
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
                        MinorPowerStanceMath.cachedVectors, eliteMobEntity.hasMinorVisualEffect(),
                        MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteMobEntity);

                return;
            }

        Object[][] multiDimensionalTrailTracker = new Object[trackAmount][eliteMobEntity.getMinorPowerCount() * individualEffectsPerTrack];

        for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
            ArrayList<Object> localObjects = new ArrayList<>();
            for (int a = 0; a < multiDimensionalTrailTracker.length; a++)
                localObjects.addAll(addAllEffects());
            for (int j = 0; j < multiDimensionalTrailTracker[0].length; j++)
                if (localObjects.get(j) != null)
                    multiDimensionalTrailTracker[i][j] = localObjects.get(j);
        }

        VisualItemProcessor visualItemProcessor = new VisualItemProcessor(multiDimensionalTrailTracker,
                MinorPowerStanceMath.cachedVectors, eliteMobEntity.hasMinorVisualEffect(),
                MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteMobEntity);

    }

    private ArrayList<Object> addObfuscatedEffects() {
        return new ArrayList<>(Arrays.asList(Particle.END_ROD));
    }

    private ArrayList<Object> addAllEffects() {

        ArrayList<Object> effects = new ArrayList<>();

        for (ElitePower elitePower : eliteMobEntity.getPowers())
            if (elitePower instanceof MinorPower)
                if (eliteMobEntity.getPower(elitePower).getTrail() != null)
                    effects.add(effectParser(eliteMobEntity.getPower(elitePower).getTrail()));

        return effects;

    }

    private Object effectParser(String powerString) {
        try {
            Material material = Material.valueOf(powerString);
            return addEffect(material);
        } catch (Exception ex) {
        }
        try {
            Particle particle = Particle.valueOf(powerString);
            return addEffect(particle);
        } catch (Exception ex) {
        }
        return null;
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
