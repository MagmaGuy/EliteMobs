package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.Invisibility;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.InvulnerabilityArrow;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.InvulnerabilityFallDamage;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.InvulnerabilityKnockback;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.BonusLoot;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.MovementSpeed;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.Taunt;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.*;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class MinorPowerPowerStance implements Listener {

    public static int trackAmount = 2;
    public static int individualEffectsPerTrack = 2;
    private ArrayList<Object> effectsList = new ArrayList<>();
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

        for (int i = 0; i < trackAmount; i++)
            for (int k = 0; k < individualEffectsPerTrack; k++)
                addAllEffects();

        if (effectsList.size() < 1)
            return;

        Object[][] multiDimensionalTrailTracker = new Object[trackAmount][effectsList.size()];

        for (int i = 0; i < trackAmount; i++)
            for (int j = 0; j < effectsList.size(); j++)
                multiDimensionalTrailTracker[i][j] = effectsList.get(j);

        VisualItemProcessor visualItemProcessor = new VisualItemProcessor(multiDimensionalTrailTracker,
                MinorPowerStanceMath.cachedVectors, eliteMobEntity.hasMinorVisualEffect(),
                MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, individualEffectsPerTrack, eliteMobEntity);

    }

    private void addAllEffects() {
        for (ElitePower elitePower : eliteMobEntity.getPowers()) {

            if (elitePower instanceof AttackArrow)
                addEffect(Material.ARROW);

            if (elitePower instanceof AttackBlinding)
                addEffect(Material.EYE_OF_ENDER);

            if (elitePower instanceof AttackFire)
                addEffect(Material.LAVA_BUCKET);

            if (elitePower instanceof AttackFireball)
                addEffect(Material.FIREBALL);

            if (elitePower instanceof AttackFreeze)
                addEffect(Material.PACKED_ICE);

            if (elitePower instanceof AttackGravity)
                addEffect(Material.ELYTRA);

            if (elitePower instanceof AttackPoison)
                addEffect(Material.EMERALD);

            if (elitePower instanceof AttackPush)
                addEffect(Material.PISTON_BASE);

            if (!VersionChecker.currentVersionIsUnder(11, 0))
                if (elitePower instanceof AttackWeakness)
                    addEffect(Material.TOTEM);

            if (elitePower instanceof AttackWeb)
                addEffect(Material.WEB);

            if (elitePower instanceof AttackWither)
                addEffect(Material.SKULL_ITEM);

            if (elitePower instanceof BonusLoot)
                addEffect(Material.CHEST);

            if (elitePower instanceof Invisibility)
                addEffect(Material.THIN_GLASS);

            if (elitePower instanceof InvulnerabilityArrow)
                addEffect(Material.SPECTRAL_ARROW);

            if (elitePower instanceof InvulnerabilityFallDamage)
                addEffect(Material.FEATHER);

            if (elitePower instanceof InvulnerabilityKnockback)
                addEffect(Material.ANVIL);

            if (elitePower instanceof MovementSpeed)
                addEffect(Material.GOLD_BOOTS);

            if (elitePower instanceof Taunt)
                addEffect(Material.JUKEBOX);

        }
    }

    private void addEffect(Material material) {

        Item item = eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(),
                new ItemStack(material));
        item.setPickupDelay(Integer.MAX_VALUE);
        if (!VersionChecker.currentVersionIsUnder(1, 11))
            item.setGravity(false);
        item.setInvulnerable(true);
        EntityTracker.registerItemVisualEffects(item);
        effectsList.add(item);

    }

    private void addEffect(Particle particle) {

        effectsList.add(particle);

    }

}
