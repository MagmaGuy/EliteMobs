package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptRelativeVectorBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collection;

public class ScriptRelativeVector {
    private final ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint;
    private ScriptTargets sourceTarget = null;
    private ScriptTargets destinationTarget = null;
    private Vector cachedVector = null;
    private Location actionLocation = null;
    private boolean sourceIsAction = false;
    private boolean destinationIsAction = false;

    public ScriptRelativeVector(ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint, ScriptRuntimeOwner runtimeOwner, Location actionLocation) {
        this.scriptRelativeVectorBlueprint = scriptRelativeVectorBlueprint;
        this.actionLocation = actionLocation;
        if (!scriptRelativeVectorBlueprint.getSourceTarget().getTargetType().equals(TargetType.ACTION_TARGET)) {
            sourceTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getSourceTarget(), runtimeOwner);
        } else {
            sourceIsAction = true;
        }
        if (!scriptRelativeVectorBlueprint.getDestinationTarget().getTargetType().equals(TargetType.ACTION_TARGET))
            destinationTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getDestinationTarget(), runtimeOwner);
        else
            destinationIsAction = true;
    }

    public Vector getVector(ScriptActionData scriptActionData) {
        if (cachedVector != null) return cachedVector;
        Location sourceLocation = endpoint(sourceIsAction, sourceTarget, scriptActionData);
        Location destinationLocation = endpoint(destinationIsAction, destinationTarget, scriptActionData);
        if (sourceLocation == null || destinationLocation == null || sourceLocation.getWorld() == null
                || !sourceLocation.getWorld().equals(destinationLocation.getWorld())) return new Vector();
        Vector vector = destinationLocation.clone().subtract(sourceLocation).toVector();
        if (scriptRelativeVectorBlueprint.isNormalize() && vector.lengthSquared() > 0) vector.normalize();
        vector.multiply(scriptRelativeVectorBlueprint.getMultiplier().getValue());
        vector.add(scriptRelativeVectorBlueprint.getOffset().getValue());
        return vector;
    }

    private Location endpoint(boolean isAction, ScriptTargets target, ScriptActionData data) {
        if (isAction) return actionLocation;
        if (target == null) return null;
        Collection<Location> locations = target.getTargetLocations(data);
        return locations.isEmpty() ? null : locations.iterator().next();
    }

    public void cacheVector(ScriptActionData scriptActionData) {
        cachedVector = getVector(scriptActionData);
    }
}
