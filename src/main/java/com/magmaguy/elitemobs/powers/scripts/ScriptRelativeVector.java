package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptRelativeVectorBlueprint;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ScriptRelativeVector {
    private ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint;
    private ScriptTargets sourceTarget;
    private ScriptTargets destinationTarget;
    private Vector cachedVector = null;

    public ScriptRelativeVector(ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint, EliteScript eliteScript) {
        this.scriptRelativeVectorBlueprint = scriptRelativeVectorBlueprint;
        sourceTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getSourceTarget(), eliteScript);
        destinationTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getDestinationTarget(), eliteScript);
    }

    public Vector getVector(ScriptActionData scriptActionData) {
        if (cachedVector != null) return cachedVector;
        if (sourceTarget == null || destinationTarget == null || sourceTarget.getTargetLocations(scriptActionData).isEmpty())
            return new Vector(0, 0, 0);
        Location sourceLocation = sourceTarget.getTargetLocations(scriptActionData).iterator().next();
        Location destinationLocation = destinationTarget.getTargetLocations(scriptActionData).iterator().next();
        Vector vector = destinationLocation.subtract(sourceLocation).toVector();
        if (scriptRelativeVectorBlueprint.isNormalize()) vector.normalize();
        vector.multiply(scriptRelativeVectorBlueprint.getMultiplier());
        vector.add(scriptRelativeVectorBlueprint.getOffset());
        return vector;
    }

    public void cacheVector(ScriptActionData scriptActionData) {
        cachedVector = getVector(scriptActionData);
    }
}
