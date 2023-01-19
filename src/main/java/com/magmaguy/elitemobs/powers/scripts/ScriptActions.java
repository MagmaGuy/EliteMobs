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


    //Used by scripts that call scripts
    public void runScripts(ScriptActionData previousScriptActiondata) {
        for (ScriptAction scriptAction : scriptActionsList)
            scriptAction.runScript(previousScriptActiondata);
    }

    //Used by landing blocks and projectiles
    public void runScripts(ScriptActionData previousActionData, Location landingLocation) {
        for (ScriptAction scriptAction : scriptActionsList)
            scriptAction.runScript(previousActionData, landingLocation);
    }

    public boolean isValid() {
        return true;
    }

}
