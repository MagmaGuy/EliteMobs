package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.powers.scripts.ScriptRuntimeOwner;
import com.magmaguy.elitemobs.powers.scripts.ScriptZone;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;

import java.util.Collections;

final class LuaScriptRuntimeOwner implements ScriptRuntimeOwner {

    private final String fileName;
    private ScriptZone scriptZone;

    LuaScriptRuntimeOwner(String fileName) {
        this.fileName = fileName;
        this.scriptZone = new ScriptZone(new ScriptZoneBlueprint(Collections.emptyMap(), "__lua__", fileName), this);
    }

    void setScriptZone(ScriptZone scriptZone) {
        this.scriptZone = scriptZone;
    }

    @Override
    public ScriptZone getScriptZone() {
        return scriptZone;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
