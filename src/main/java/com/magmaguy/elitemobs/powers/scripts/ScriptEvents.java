package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptEventsBlueprint;


public class ScriptEvents {
    private final ScriptEventsBlueprint scriptEventsBlueprint;

    public ScriptEvents(ScriptEventsBlueprint eventsBlueprint) {
        this.scriptEventsBlueprint = eventsBlueprint;
    }

    public boolean isTargetEvent(Class clazz) {
        return scriptEventsBlueprint.getEvents().contains(clazz);
    }

    public boolean isValid() {
        //return !events.isEmpty(); todo: parse failed attempts, empty right now still means they can be called by other scripts
        return true;
    }
}
