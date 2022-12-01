package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.List;

public class ScriptActionData {
    @Getter
    @Setter
    private Collection<Location> locations = null;
    @Getter
    @Setter
    private List<Shape> cachedShapes = null;
    @Getter
    @Setter
    private EliteEntity eliteEntity;
    @Getter
    @Setter
    private LivingEntity directTarget = null;
    @Getter
    @Setter
    private Location landingLocation = null;

    public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget) {
        this.eliteEntity = eliteEntity;
        this.directTarget = directTarget;
    }

    public ScriptActionData(EliteEntity eliteEntity, Location landingLocation) {
        this.eliteEntity = eliteEntity;
        this.landingLocation = landingLocation;
    }
}
