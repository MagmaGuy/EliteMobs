package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Consumer;


public class VisualDisplay {

    public static ArmorStand generateTemporaryArmorStand(Location location, String customName) {
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
        EntityTracker.registerVisualEffects(visualArmorStand);
        return visualArmorStand;
    }

    public static TextDisplay generateTemporaryTextDisplay(Location location, String customName) {
        TextDisplay visualArmorStand = location.getWorld().spawn(location, TextDisplay.class, new Consumer<TextDisplay>() {
            @Override
            public void accept(TextDisplay textDisplay) {
                textDisplay.setText(ChatColorConverter.convert(customName));
                textDisplay.setPersistent(false);
                textDisplay.setInterpolationDelay(0);
                textDisplay.setInterpolationDuration(0);
                textDisplay.setBillboard(Display.Billboard.VERTICAL);
                textDisplay.setShadowed(false);
            }
        });
        EntityTracker.registerVisualEffects(visualArmorStand);
        return visualArmorStand;
    }

}
