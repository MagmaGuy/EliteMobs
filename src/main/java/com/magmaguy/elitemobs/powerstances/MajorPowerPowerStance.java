package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerPowerStance implements Listener, AutoCloseable {

    public static int trackAmount = 2;
    public static int individualEffectsPerTrack = 2;
    private EliteEntity eliteEntity;
    private final List<Item> spawnedItems = new ArrayList<>();
    private VisualItemProcessor visualItemProcessor;

    public MajorPowerPowerStance(EliteEntity eliteEntity) {
        this(eliteEntity, ignored -> { });
    }

    public MajorPowerPowerStance(EliteEntity eliteEntity, Consumer<Runnable> cleanupRegistrar) {

        if (!MobCombatSettingsConfig.isEnableVisualEffectsForNaturalMobs())
            return;
        if (MobCombatSettingsConfig.isDisableVisualEffectsForSpawnerMobs() && !eliteEntity.isNaturalEntity())
            return;
        //The ring drops item entities in the elite's world, so there is nothing to build before the elite exists
        if (eliteEntity.getLivingEntity() == null)
            return;

        this.eliteEntity = eliteEntity;
        Objects.requireNonNull(cleanupRegistrar, "cleanupRegistrar").accept(this::close);

        if (eliteEntity.isMajorVisualEffect()) return;
        eliteEntity.setMajorVisualEffect(true);

        if (eliteEntity.getMajorPowerCount() < 1)
            return;

        /*
        Obfuscate powers to prevent TPS loss
         */
        if (MobCombatSettingsConfig.isObfuscateMobPowers())
            if (eliteEntity.isVisualEffectObfuscated()) {
                Object[][] multiDimensionalTrailTracker = new Object[trackAmount][individualEffectsPerTrack];

                for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
                    ArrayList<Object> localObjects = new ArrayList<>();
                    for (int a = 0; a < multiDimensionalTrailTracker.length; a++)
                        localObjects.addAll(addObfuscatedEffects());
                    for (int j = 0; j < multiDimensionalTrailTracker[0].length; j++)
                        if (j < localObjects.size() && localObjects.get(j) != null)
                            multiDimensionalTrailTracker[i][j] = localObjects.get(j);

                }

                visualItemProcessor = new VisualItemProcessor(multiDimensionalTrailTracker,
                        MajorPowerStanceMath.cachedVectors, eliteEntity.isMajorVisualEffect(),
                        MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteEntity);

                return;
            }

        Object[][] multiDimensionalTrailTracker = new Object[trackAmount][eliteEntity.getMajorPowerCount() * individualEffectsPerTrack];

        for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
            ArrayList<Object> localObjects = new ArrayList<>();
            for (int a = 0; a < multiDimensionalTrailTracker.length; a++)
                localObjects.addAll(addAllEffects());
            for (int j = 0; j < multiDimensionalTrailTracker[0].length; j++)
                if (j < localObjects.size() && localObjects.get(j) != null)
                    multiDimensionalTrailTracker[i][j] = localObjects.get(j);
        }

        visualItemProcessor = new VisualItemProcessor(multiDimensionalTrailTracker,
                MajorPowerStanceMath.cachedVectors, eliteEntity.isMajorVisualEffect(),
                MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, eliteEntity);

    }

    private ArrayList<Object> addObfuscatedEffects() {
        return new ArrayList<>(List.of(Particle.END_ROD));
    }

    private ArrayList<Object> addAllEffects() {

        ArrayList<Object> effects = new ArrayList<>();

        for (String trail : PowerStanceEffectSelector.selectTrails(eliteEntity, true))
            effects.add(effectParser(trail));

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
        spawnedItems.add(item);
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setGravity(false);
        item.setInvulnerable(true);
        EntityTracker.registerVisualEffects(item);
        return item;

    }

    private Object addEffect(Particle particle) {
        return particle;
    }

    @Override
    public void close() {
        if (visualItemProcessor != null) visualItemProcessor.close();
        for (Item item : spawnedItems) {
            item.remove();
            EntityTracker.unregister(item, com.magmaguy.elitemobs.api.internal.RemovalReason.EFFECT_TIMEOUT);
        }
        spawnedItems.clear();
    }

}
