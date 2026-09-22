package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.magmacore.scripting.zones.Shape;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

public class ScriptActionData {
    // Targets are reusable definitions. Waits, repeats and their snapshots belong to this invocation.
    private final Map<ScriptTargets, List<Location>> locationSnapshots = new IdentityHashMap<>();
    private final Map<ScriptTargets, List<?>> inheritedTargets = new IdentityHashMap<>();

    List<Location> locationSnapshot(ScriptTargets targets) {
        List<Location> snapshot = locationSnapshots.get(targets);
        return snapshot == null ? null : snapshot.stream().map(Location::clone)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }

    void captureLocations(ScriptTargets targets, Collection<Location> locations) {
        locationSnapshots.putIfAbsent(targets, locations.stream().map(Location::clone).toList());
    }

    List<?> inheritedTargets(ScriptTargets targets) {
        List<?> snapshot = inheritedTargets.get(targets);
        return snapshot == null ? null : copyTargets(snapshot);
    }

    void captureInheritedTargets(ScriptTargets targets, List<?> values) {
        inheritedTargets.put(targets, copyTargets(values));
    }

    private static List<?> copyTargets(List<?> targets) {
        return targets.stream().map(target -> target instanceof Location location ? location.clone() : target)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }

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

    /** Nested RUN_SCRIPT actions retain the event that admitted the original action. */
    boolean originatesFromDeath() {
        for (ScriptActionData data = this; data != null; data = data.inheritedScriptActionData) {
            if (data.event != null) return data.event instanceof EliteMobDeathEvent;
        }
        return false;
    }

    /** Each action owns its targets, so this identifies a RUN_SCRIPT cycle without another registry. */
    boolean repeatsActionInChain() {
        for (ScriptActionData data = inheritedScriptActionData; data != null; data = data.inheritedScriptActionData) {
            if (data.scriptTargets == scriptTargets) return true;
        }
        return false;
    }

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
    private List<Shape> shapesCachedByTarget;


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

    //Used by actions that call scripts
    public ScriptActionData(ScriptTargets scriptTargets, ScriptZone scriptZone, ScriptActionData inheritedScriptActionData) {
        this.eliteEntity = inheritedScriptActionData.getEliteEntity();
        this.directTarget = inheritedScriptActionData.getDirectTarget();
        this.scriptTargets = scriptTargets;
        //This stores the cache shape
        this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
        this.scriptZone = scriptZone;
        this.inheritedScriptActionData = inheritedScriptActionData;
        this.landingLocation = inheritedScriptActionData.getLandingLocation();
    }

    //Used for the zone enter and leave, can't use direct targets
    public ScriptActionData(EliteEntity eliteEntity, ScriptTargets scriptTargets, ScriptZone scriptZone) {
        this.eliteEntity = eliteEntity;
        this.scriptTargets = scriptTargets;
        //This stores the cache shape
        this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
        this.scriptZone = scriptZone;
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
