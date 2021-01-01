package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;

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
        armorStand.setMetadata(
                MetadataHandler.ARMOR_STAND,
                new FixedMetadataValue(MetadataHandler.PLUGIN, true));
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        if (armorStand != null)
            armorStand.removeMetadata(MetadataHandler.ARMOR_STAND, MetadataHandler.PLUGIN);
    }

}
