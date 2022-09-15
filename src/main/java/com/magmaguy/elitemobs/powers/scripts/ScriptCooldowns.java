package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;

import java.util.List;

public class ScriptCooldowns {
    @Getter
    private int localCooldown = 0;
    @Getter
    private int globalCooldown = 0;

    public ScriptCooldowns(List<String> values, String scriptName, ElitePower elitePower) {
        for (String entry : values) {
            String[] subentries = entry.split("=");
            switch (subentries[0].toLowerCase()) {
                case "local_cooldown":
                    try {
                        localCooldown = Integer.parseInt(subentries[1]);
                    } catch (Exception exception) {
                        new WarningMessage("Failed to get valid local_cooldown for entry " + entry + " in " + scriptName + " !");
                    }
                    break;
                case "global_cooldown":
                    try {
                        globalCooldown = Integer.parseInt(subentries[1]);
                    } catch (Exception exception) {
                        new WarningMessage("Failed to get valid global_cooldown for entry " + entry + " in " + scriptName + " !");
                    }
                    break;
                default:
                    new WarningMessage("Invalid cooldown entry for value " + entry + " in " + scriptName);
                    break;
            }
        }
        elitePower.setGlobalCooldownTime(globalCooldown);
        elitePower.setPowerCooldownTime(localCooldown);
    }
}
