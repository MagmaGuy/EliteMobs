package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class VisualEffectsEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, Item> visualEffectEntities = new HashMap<>();

    public UUID uuid;
    public Item entity;

    public VisualEffectsEntityTracker(UUID uuid, Item item) {
        super(uuid, item, true, true, visualEffectEntities);
        this.uuid = uuid;
        this.entity = item;
        visualEffectEntities.put(uuid, item);
        item.setMetadata(
                MetadataHandler.VISUAL_EFFECT,
                new FixedMetadataValue(MetadataHandler.PLUGIN, true));
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        if (entity == null) return;
        entity.removeMetadata(
                MetadataHandler.VISUAL_EFFECT,
                MetadataHandler.PLUGIN);
        entity.remove();
    }

}
