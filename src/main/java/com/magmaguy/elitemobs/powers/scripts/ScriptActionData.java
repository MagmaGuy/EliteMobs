package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.List;

public class ScriptActionData {
    @Getter
    private final TargetType targetType;
    @Getter
    private final ScriptTargets scriptTargets;
    @Getter
    @Setter
    private Collection<Location> locations = null;
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
    private ScriptZone scriptZone = null;
    @Getter
    @Setter
    private ScriptActionData inheritedScriptActionData = null;
    @Getter
    private Event event;

    //previousEntityTargets and previousLocationTargets only have values when other scripts call this script
    //in which case the targets are inherited by this script so they can be reused
    @Setter
    @Getter
    private Collection<LivingEntity> previousEntityTargets;
    @Setter
    @Getter
    private Collection<Location> previousLocationTargets;

    //This allows shapes to be cached in a way that is isolated to each script without contaminating scripts
    @Getter
    @Setter
    private List<Shape> shapesChachedByTarget;


    public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget, ScriptTargets scriptTargets, Event event) {
        this.eliteEntity = eliteEntity;
        this.directTarget = directTarget;
        this.scriptTargets = scriptTargets;
        //This stores the cache shape
        this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
        this.event = event;
    }

    public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget, ScriptTargets scriptTargets, ScriptZone scriptZone, Event event) {
        this.eliteEntity = eliteEntity;
        this.directTarget = directTarget;
        this.scriptTargets = scriptTargets;
        //This stores the cache shape
        this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
        this.scriptZone = scriptZone;
        this.event = event;
    }

    public ScriptActionData(ScriptTargets scriptTargets, ScriptZone scriptZone, ScriptActionData inheritedScriptActionData) {
        this.eliteEntity = inheritedScriptActionData.getEliteEntity();
        this.directTarget = inheritedScriptActionData.getDirectTarget();
        this.scriptTargets = scriptTargets;
        //This stores the cache shape
        this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
        this.scriptZone = scriptZone;
        this.inheritedScriptActionData = inheritedScriptActionData;
    }

    //For data with landing locations
    public ScriptActionData(ScriptTargets scriptTargets, ScriptZone scriptZone, ScriptActionData inheritedScriptActionData, Location landingLocation) {
        this.eliteEntity = inheritedScriptActionData.getEliteEntity();
        this.directTarget = inheritedScriptActionData.getDirectTarget();
        this.scriptTargets = scriptTargets;
        //This stores the cache shape
        this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
        this.scriptZone = scriptZone;
        this.inheritedScriptActionData = inheritedScriptActionData;
        this.landingLocation = landingLocation;
    }

    //For data called by other scripts
    public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget, ScriptTargets scriptTargets, Collection<LivingEntity> previousEntityTargets, Collection<Location> previousLocationTargets) {
        this.eliteEntity = eliteEntity;
        this.directTarget = directTarget;
        this.scriptTargets = scriptTargets;
        //This stores the cache shape
        this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
        this.previousEntityTargets = previousEntityTargets;
        this.previousLocationTargets = previousLocationTargets;
    }
}
