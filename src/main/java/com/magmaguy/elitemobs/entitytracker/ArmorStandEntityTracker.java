package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import org.bukkit.entity.ArmorStand;

import java.util.HashMap;
import java.util.UUID;

public class ArmorStandEntityTracker extends TrackedEntity implements AbstractTrackedEntity {
    public static HashMap<UUID, ArmorStand> armorStands = new HashMap<>();
    public UUID uuid;
    public ArmorStand armorStand;

    public ArmorStandEntityTracker(UUID uuid, ArmorStand armorStand) {
        super(uuid, armorStand, true, true, armorStands);
        this.uuid = uuid;
        this.armorStand = armorStand;
        armorStands.put(uuid, armorStand);
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
    }

}
