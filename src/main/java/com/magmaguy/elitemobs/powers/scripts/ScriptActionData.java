package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

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
    @Getter
    private final Target target;
    @Getter
    private Event event;

    //previousEntityTargets and previousLocationTargets only have values when other scripts call this script
    //in which case the targets are inherited by this script so they can be reused
    @Setter
    @Getter
    private Collection<Entity> previousEntityTargets;
    @Setter
    @Getter
    private Collection<Location> previousLocationTargets;


    public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget, Target target, Event event) {
        this.eliteEntity = eliteEntity;
        this.directTarget = directTarget;
        this.target = target;
        this.event = event;
    }

    //For data with landing locations
    public ScriptActionData(EliteEntity eliteEntity, Location landingLocation, Target target) {
        this.eliteEntity = eliteEntity;
        this.landingLocation = landingLocation;
        this.target = target;
    }

    //For data called by other scripts
    public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget, Target target, Collection<Entity> previousEntityTargets, Collection<Location> previousLocationTargets) {
        this.eliteEntity = eliteEntity;
        this.directTarget = directTarget;
        this.target = target;
        this.previousEntityTargets = previousEntityTargets;
        this.previousLocationTargets = previousLocationTargets;
    }
}
