package com.magmaguy.elitemobs.powers;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class ProjectileLocationGenerator {

    public static Location generateLocation(LivingEntity targetter, LivingEntity targetted) {
        return targetter.getLocation().clone().add(0, 1, 0).add(targetted.getLocation().clone().subtract(targetter.getLocation().clone()).toVector().normalize()).setDirection(targetted.getLocation().clone().subtract(targetter.getLocation()).toVector());
    }

}
