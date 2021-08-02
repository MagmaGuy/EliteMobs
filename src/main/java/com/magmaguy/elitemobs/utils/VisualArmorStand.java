package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Consumer;


public class VisualArmorStand {

    public static ArmorStand VisualArmorStand(Location location, String customName) {
        ArmorStand visualArmorStand = location.getWorld().spawn(location, ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand armorStand) {
                armorStand.setVisible(false);
                armorStand.setMarker(true);
                armorStand.setCustomName(ChatColorConverter.convert(customName));
                armorStand.setCustomNameVisible(true);
                armorStand.setGravity(false);
                armorStand.setRemoveWhenFarAway(true);
                armorStand.setPersistent(false);
            }
        });
        EntityTracker.registerArmorStands(visualArmorStand);
        return visualArmorStand;
    }

}
