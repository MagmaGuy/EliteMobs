package com.magmaguy.elitemobs.runnables;

import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

import static com.magmaguy.elitemobs.mobcustomizer.displays.DisplayMob.livingEntityList;

public class ReapplyDisplayEffects extends BukkitRunnable {

    @Override
    public void run() {

        if (livingEntityList.isEmpty()) return;

        Iterator iterator = livingEntityList.iterator();

        while (iterator.hasNext()) {

            Object currentEntity = iterator.next();

            if (currentEntity == null) {

                iterator.remove();

            } else {

                LivingEntity iteratedEntity = (LivingEntity) currentEntity;

                if (iteratedEntity.isDead() || !iteratedEntity.isValid()) {

                    iterator.remove();

                }

            }

        }

        for (LivingEntity livingEntity : livingEntityList) {
            if (livingEntity.isValid()) {
                MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance();
                majorPowerPowerStance.itemEffect(livingEntity);
                MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
                minorPowerPowerStance.itemEffect(livingEntity);
            }
        }

    }

}
