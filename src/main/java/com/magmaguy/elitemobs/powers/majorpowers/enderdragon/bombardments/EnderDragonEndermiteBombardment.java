package com.magmaguy.elitemobs.powers.majorpowers.enderdragon.bombardments;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EnderDragonEndermiteBombardment extends Bombardment {
    public EnderDragonEndermiteBombardment() {
        super(PowersConfig.getPower("ender_dragon_endermite_bombardment.yml"));
    }

    @Override
    public void taskBehavior(EliteMobEntity eliteMobEntity) {
        try {

            Vector direction = eliteMobEntity.getLivingEntity().getLocation().getDirection();

            float rotation = (float) (Math.atan2(direction.getX(), direction.getZ()) * 180 / Math.PI);

            Vector direction1 = new Vector(1, -4, 0);
            Vector direction2 = new Vector(-1, -4, 0);

            direction1 = direction1.rotateAroundY(rotation);
            direction2 = direction2.rotateAroundY(rotation);

            CustomBossEntity customBossEntity1 = CustomBossEntity.constructCustomBoss("binder_of_worlds_phase_1_endermite_reinforcement.yml",
                    eliteMobEntity.getLivingEntity().getLocation().clone().add(direction1));
            customBossEntity1.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 5, 0));

            eliteMobEntity.eliteReinforcementEntities.add(customBossEntity1);

            CustomBossEntity customBossEntity2 = CustomBossEntity.constructCustomBoss("binder_of_worlds_phase_1_endermite_reinforcement.yml",
                    eliteMobEntity.getLivingEntity().getLocation().clone().add(direction2));
            customBossEntity2.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 5, 0));

            eliteMobEntity.eliteReinforcementEntities.add(customBossEntity2);

        } catch (Exception ex) {
            new WarningMessage("Failed to spawn binder of world's reinforcement endermite!");
        }
    }

}
