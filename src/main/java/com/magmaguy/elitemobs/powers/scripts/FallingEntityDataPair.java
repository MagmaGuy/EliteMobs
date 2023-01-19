package com.magmaguy.elitemobs.powers.scripts;

import lombok.Getter;

public class FallingEntityDataPair {
    @Getter
    private final ScriptAction scriptAction;
    @Getter
    private final ScriptActionData scriptActionData;

    public FallingEntityDataPair(ScriptAction scriptAction, ScriptActionData scriptActionData) {
        this.scriptAction = scriptAction;
        this.scriptActionData = scriptActionData;
    }
}
