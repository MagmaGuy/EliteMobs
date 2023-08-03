package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptRelativeVectorBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.Developer;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ScriptRelativeVector {
    private final ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint;
    private ScriptTargets sourceTarget = null;
    private ScriptTargets destinationTarget = null;
    private Vector cachedVector = null;
    private Location actionLocation = null;
    private boolean sourceIsAction = false;
    private boolean destinationIsAction = false;

    public ScriptRelativeVector(ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint, EliteScript eliteScript, Location actionLocation) {
        this.scriptRelativeVectorBlueprint = scriptRelativeVectorBlueprint;
        Developer.message("relative vector initialized for " + eliteScript.getFileName());
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
        Developer.message("1");
        if (cachedVector != null) return cachedVector;
        Developer.message("2");
        Location sourceLocation = null;
        if (sourceIsAction) {
            sourceLocation = actionLocation.clone();
        }
        else if (sourceTarget != null && !sourceTarget.getTargetLocations(scriptActionData).isEmpty())
            sourceLocation = sourceTarget.getTargetLocations(scriptActionData).iterator().next();
        else
            return new Vector(0, 0, 0);
        Developer.message("3");

        Location destinationLocation = null;

        if (destinationIsAction)
            destinationLocation = actionLocation.clone();
        else if (destinationTarget != null && !destinationTarget.getTargetLocations(scriptActionData).isEmpty())
            destinationLocation = destinationTarget.getTargetLocations(scriptActionData).iterator().next().clone();
        else
            return new Vector(0, 0, 0);
        Developer.message("4");
        Developer.message("Source: "+sourceLocation + " destination " + destinationLocation);

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
