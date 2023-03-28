package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptRelativeVectorBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ScriptRelativeVector {
    private ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint;
    private ScriptTargets sourceTarget = null;
    private ScriptTargets destinationTarget = null;
    private Vector cachedVector = null;
    private Location actionLocation = null;
    private boolean sourceIsAction = false;
    private boolean destinationIsAction = false;

    public ScriptRelativeVector(ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint, EliteScript eliteScript, Location actionLocation) {
        this.scriptRelativeVectorBlueprint = scriptRelativeVectorBlueprint;
        this.actionLocation = actionLocation;
        if (!scriptRelativeVectorBlueprint.getSourceTarget().getTargetType().equals(TargetType.ACTION_TARGET)) {
            sourceTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getSourceTarget(), eliteScript);
        }
        else {
            sourceIsAction = true;
        }
        if (!scriptRelativeVectorBlueprint.getDestinationTarget().getTargetType().equals(TargetType.ACTION_TARGET))
            destinationTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getDestinationTarget(), eliteScript);
        else
            destinationIsAction = true;
    }

    public Vector getVector(ScriptActionData scriptActionData) {
        if (cachedVector != null) return cachedVector;
        Location sourceLocation = null;
        if (sourceIsAction) {
            sourceLocation = actionLocation.clone();
        }
        else if (sourceTarget != null && !sourceTarget.getTargetLocations(scriptActionData).isEmpty())
            sourceLocation = sourceTarget.getTargetLocations(scriptActionData).iterator().next();
        else
            return new Vector(0, 0, 0);

        Location destinationLocation = null;

        if (destinationIsAction)
            destinationLocation = actionLocation.clone();
        else if (destinationTarget != null && !destinationTarget.getTargetLocations(scriptActionData).isEmpty())
            destinationLocation = destinationTarget.getTargetLocations(scriptActionData).iterator().next().clone();
        else
            return new Vector(0, 0, 0);

        Vector vector = destinationLocation.clone().subtract(sourceLocation).toVector();
        if (scriptRelativeVectorBlueprint.isNormalize()) vector.normalize();
        vector.multiply(scriptRelativeVectorBlueprint.getMultiplier());
        vector.add(scriptRelativeVectorBlueprint.getOffset());
        return vector;
    }

    public void cacheVector(ScriptActionData scriptActionData) {
        cachedVector = getVector(scriptActionData);
    }
}
