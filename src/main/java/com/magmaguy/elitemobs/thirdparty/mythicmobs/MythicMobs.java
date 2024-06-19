package com.magmaguy.elitemobs.thirdparty.mythicmobs;


import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class MythicMobs {
    public static Entity spawn(Location location, String name, int level) {
        MythicMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(name).orElse(null);
        if (mythicMob != null) {
            ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), level);
            return activeMob.getEntity().getBukkitEntity();
        }
        return null;
    }
}
