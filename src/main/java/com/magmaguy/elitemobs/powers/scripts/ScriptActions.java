package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionsBlueprint;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptActions {
    @Getter
    private final List<ScriptAction> scriptActionsList = new ArrayList();

    public ScriptActions(ScriptActionsBlueprint scriptActionsBlueprint, Map<String, EliteScript> eliteScriptMap, EliteScript eliteScript) {
        scriptActionsBlueprint.getScriptActionsBlueprintList().forEach(scriptActionBlueprint -> scriptActionsList.add(new ScriptAction(scriptActionBlueprint, eliteScriptMap, eliteScript)));
    }

    public void runScripts(EliteEntity eliteEntity, LivingEntity directTarget, Event event) {
        for (ScriptAction scriptAction : scriptActionsList)
            scriptAction.runScript(eliteEntity, directTarget, event);
    }

    public void runScripts(EliteEntity eliteEntity, Location landingLocation) {
        for (ScriptAction scriptAction : scriptActionsList)
            scriptAction.runScript(eliteEntity, landingLocation);
    }

    public boolean isValid() {
        return true;
    }

}
