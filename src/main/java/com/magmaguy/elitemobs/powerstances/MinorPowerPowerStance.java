package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.*;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.*;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.*;
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

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS))
            return;
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS)
                && !eliteMobEntity.isNaturalEntity())
            return;

        this.eliteMobEntity = eliteMobEntity;
        if (eliteMobEntity.hasMinorVisualEffect()) return;
        eliteMobEntity.setHasMinorVisualEffect(true);

        if (eliteMobEntity.getMinorPowerCount() < 1)
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

        for (ElitePower elitePower : eliteMobEntity.getPowers()) {

            if (elitePower instanceof AttackArrow)
                effects.add(addEffect(Material.ARROW));

            if (elitePower instanceof AttackBlinding)
                effects.add(addEffect(Material.EYE_OF_ENDER));

            if (elitePower instanceof AttackConfusing)
                effects.add(addEffect(Particle.SPELL_MOB));

            if (elitePower instanceof AttackFire)
                effects.add(addEffect(Material.LAVA_BUCKET));

            if (elitePower instanceof AttackFireball)
                effects.add(addEffect(Material.FIREBALL));

            if (elitePower instanceof AttackFreeze)
                effects.add(addEffect(Material.PACKED_ICE));

            if (elitePower instanceof AttackGravity)
                effects.add(addEffect(Material.ELYTRA));

            if (elitePower instanceof AttackPoison)
                effects.add(addEffect(Material.EMERALD));

            if (elitePower instanceof AttackPush)
                effects.add(addEffect(Material.PISTON_BASE));

            if (!VersionChecker.currentVersionIsUnder(11, 0))
                if (elitePower instanceof AttackWeakness)
                    effects.add(addEffect(Material.TOTEM));

            if (elitePower instanceof AttackWeb)
                effects.add(addEffect(Material.WEB));

            if (elitePower instanceof AttackWither)
                effects.add(addEffect(Material.SKULL_ITEM));

            if (elitePower instanceof BonusLoot)
                effects.add(addEffect(Material.CHEST));

            if (elitePower instanceof MovementSpeed)
                effects.add(addEffect(Material.GOLD_BOOTS));

            if (elitePower instanceof Taunt)
                effects.add(addEffect(Material.JUKEBOX));

            if (elitePower instanceof Invisibility)
                effects.add(addEffect(Material.THIN_GLASS));

            if (elitePower instanceof InvulnerabilityArrow)
                effects.add(addEffect(Material.SPECTRAL_ARROW));

            if (elitePower instanceof InvulnerabilityFallDamage)
                effects.add(addEffect(Material.FEATHER));

            if (elitePower instanceof InvulnerabilityFire)
                effects.add(addEffect(Particle.FLAME));

            if (elitePower instanceof InvulnerabilityKnockback)
                effects.add(addEffect(Material.ANVIL));

            if (elitePower instanceof Corpse)
                effects.add(addEffect(Material.BONE_BLOCK));

            if (elitePower instanceof MoonWalk)
                effects.add(addEffect(Material.SLIME_BLOCK));

            if (elitePower instanceof Implosion)
                effects.add(addEffect(Material.SLIME_BALL));

            if (elitePower instanceof AttackVacuum)
                effects.add(addEffect(Material.LEASH));

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
