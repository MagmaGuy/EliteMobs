package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EliteScript extends ElitePower {
    private final String scriptName;

    //Parse from power file
    private final ScriptEvents scriptEvents;
    private final ScriptConditions scriptConditions;
    private final ScriptActions scriptActions;
    private final ScriptCooldowns scriptCooldowns;
    public EliteScript(ConfigurationSection configurationSection, String scriptName) {
        super(configurationSection, scriptName);
        this.scriptName = scriptName;
        scriptEvents = new ScriptEvents(configurationSection.getStringList("Events"), scriptName);
        scriptConditions = new ScriptConditions(configurationSection.getStringList("Conditions"), scriptName);
        scriptActions = new ScriptActions(configurationSection.getMapList("Actions"), scriptName);
        scriptCooldowns = new ScriptCooldowns(configurationSection.getStringList("Cooldowns"), scriptName, this);
        if (!scriptEvents.isValid()) {
            new WarningMessage("Script does not have valid Events for entry " + scriptName + "!");
            return;
        }
        if (!scriptActions.isValid()) {
            new WarningMessage("Script does not have valid Actions for entry " + scriptName + "!");
        }
    }

    //Parse from boss config
    public static List<EliteScript> parseBossScripts(ConfigurationSection section) {
        if (section == null) return Collections.emptyList();
        List<EliteScript> eliteScripts = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            eliteScripts.add(new EliteScript(section.getConfigurationSection(key), key));
        }
        return eliteScripts;
    }

    public void check(Class eventClass, EliteEntity eliteEntity, Player player) {
        //Check if the event is relevant to the script
        if (!scriptEvents.isTargetEvent(eventClass)) return;
        //Check if the event conditions are met
        if (!scriptConditions.meetsConditions()) return;
        //Let's do some actions
        scriptActions.runScripts(eliteEntity, player);
        //Cooldowns time
        doCooldown(eliteEntity);
    }

}
