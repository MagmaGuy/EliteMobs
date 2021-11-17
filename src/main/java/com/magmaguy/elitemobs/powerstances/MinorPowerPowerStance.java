package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
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
    private EliteEntity eliteEntity;

    //Secondary effect item processing
    public MinorPowerPowerStance(EliteEntity eliteEntity) {

        if (!MobCombatSettingsConfig.enableVisualEffectsForNaturalMobs)
            return;
        if (MobCombatSettingsConfig.disableVisualEffectsForSpawnerMobs && !eliteEntity.isNaturalEntity())
            return;

        this.eliteEntity = eliteEntity;
        if (eliteEntity.isMinorVisualEffect()) return;
        eliteEntity.setMinorVisualEffect(true);

        if (eliteEntity.getMinorPowerCount() < 1)
            return;

        /*
        Obfuscate powers to prevent TPS loss
         */
        if (MobCombatSettingsConfig.obfuscateMobPowers)
            if (eliteEntity.isVisualEffectObfuscated()) {
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
                        MinorPowerStanceMath.cachedVectors, eliteEntity.isMinorVisualEffect(),
                        MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteEntity);

                return;
            }

        Object[][] multiDimensionalTrailTracker = new Object[trackAmount][eliteEntity.getMinorPowerCount() * individualEffectsPerTrack];

        for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
            ArrayList<Object> localObjects = new ArrayList<>();
            for (int a = 0; a < multiDimensionalTrailTracker.length; a++)
                localObjects.addAll(addAllEffects());
            for (int j = 0; j < localObjects.size(); j++)
                if (localObjects.get(j) != null)
                    multiDimensionalTrailTracker[i][j] = localObjects.get(j);
        }

        VisualItemProcessor visualItemProcessor = new VisualItemProcessor(multiDimensionalTrailTracker,
                MinorPowerStanceMath.cachedVectors, eliteEntity.isMinorVisualEffect(),
                MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteEntity);

    }

    private ArrayList<Object> addObfuscatedEffects() {
        return new ArrayList<>(Arrays.asList(Particle.END_ROD));
    }

    private ArrayList<Object> addAllEffects() {

        ArrayList<Object> effects = new ArrayList<>();

        for (ElitePower elitePower : eliteEntity.getElitePowers())
            if (elitePower instanceof MinorPower)
                if (eliteEntity.getPower(elitePower).getTrail() != null)
                    effects.add(effectParser(eliteEntity.getPower(elitePower).getTrail()));

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

        Item item = eliteEntity.getLivingEntity().getWorld().dropItem(eliteEntity.getLivingEntity().getLocation(),
                new ItemStack(material));
        item.setPickupDelay(Integer.MAX_VALUE);
        if (!VersionChecker.serverVersionOlderThan(1, 11))
            item.setGravity(false);
        item.setInvulnerable(true);
        EntityTracker.registerItemVisualEffects(item);
        return item;

    }

    private Object addEffect(Particle particle) {
        return particle;
    }

}
