package com.magmaguy.elitemobs.api.internal;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.dungeons.SchematicDungeonPackage;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class NewSchematicPackageRelativeBossLocationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final SchematicDungeonPackage schematicDungeonPackage;
    @Getter
    private final Location relativeLocation;
    @Getter
    private final Location realLocation;
    @Getter
    private final CustomBossesConfigFields customBossesConfigFields;
    private boolean isCancelled = false;

    public NewSchematicPackageRelativeBossLocationEvent(SchematicDungeonPackage schematicDungeonPackage,
                                                        Location relativeLocation,
                                                        Location realLocation,
                                                        CustomBossesConfigFields customBossesConfigFields) {
        this.schematicDungeonPackage = schematicDungeonPackage;
        this.relativeLocation = relativeLocation;
        this.realLocation = realLocation;
        this.customBossesConfigFields = customBossesConfigFields;
        if (relativeLocation == null) setCancelled(true);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
