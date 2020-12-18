package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;


public class VisualArmorStand {

    public static ArmorStand VisualArmorStand(Location location, String customName) {
        ArmorStand visualArmorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        EntityTracker.registerArmorStands(visualArmorStand);
        visualArmorStand.setVisible(false);
        visualArmorStand.setMarker(true);
        visualArmorStand.setCustomName(ChatColorConverter.convert(customName));
        visualArmorStand.setCustomNameVisible(true);
        visualArmorStand.setGravity(false);
        visualArmorStand.setRemoveWhenFarAway(true);
        return visualArmorStand;
    }

}
