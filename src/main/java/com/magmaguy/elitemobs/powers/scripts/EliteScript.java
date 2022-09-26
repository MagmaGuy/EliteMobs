package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class EliteScript extends ElitePower {
    protected final ScriptActions scriptActions;
    private final String scriptName;
    //Parse from power file
    private final ScriptEvents scriptEvents;
    private final ScriptCooldowns scriptCooldowns;
    protected Map<String, EliteScript> eliteScriptMap = new HashMap<>();
    private ScriptConditions scriptConditions = null;

    public EliteScript(ConfigurationSection configuration, String scriptName, Map<String, EliteScript> eliteScriptMap) {
        super(scriptName);
        this.scriptName = scriptName;
        this.eliteScriptMap = eliteScriptMap;
        scriptEvents = new ScriptEvents(configuration.getStringList("Events"), scriptName);
        Map<String, Object> conditionsSection = (Map<String, Object>) configuration.get("Conditions");
        if (conditionsSection != null)
            scriptConditions = new ScriptConditions(configuration.getValues(false), scriptName);
        scriptActions = new ScriptActions(configuration.getMapList("Actions"), scriptName, this);
        if (configuration.getConfigurationSection("Cooldowns") != null)
            scriptCooldowns = new ScriptCooldowns(configuration.getConfigurationSection("Cooldowns").getValues(false), scriptName, this);
        else scriptCooldowns = null;
        if (!scriptEvents.isValid()) {
            new WarningMessage("Script does not have valid Events for entry " + scriptName + "!");
            return;
        }
        if (!scriptActions.isValid()) {
            new WarningMessage("Script does not have valid Actions for entry " + scriptName + "!");
        }
    }

    //Parse from boss config
    public static List<EliteScript> parseBossScripts(ConfigurationSection configurationSection) {
        if (configurationSection == null) return Collections.emptyList();
        List<EliteScript> eliteScripts = new ArrayList<>();
        Map<String, EliteScript> permanentMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : configurationSection.getValues(false).entrySet()) {
            EliteScript eliteScript = new EliteScript(configurationSection.getConfigurationSection(entry.getKey()), entry.getKey(), permanentMap);
            eliteScripts.add(eliteScript);
            permanentMap.put(entry.getKey(), eliteScript);
        }
        return eliteScripts;
    }

    /**
     * Used by events that call scripts
     *
     * @param eventClass
     * @param eliteEntity
     * @param player
     */
    public void check(Class eventClass, EliteEntity eliteEntity, Player player) {
        //If the script uses the cooldown system then it should respect if the boss is in a global or local cooldown state
        if (scriptCooldowns != null && super.isInCooldown(eliteEntity)) return;
        //Check if the event is relevant to the script
        if (!scriptEvents.isTargetEvent(eventClass)) return;
        //Check if the event conditions are met
        if (scriptConditions != null && !scriptConditions.meetsConditions(eliteEntity, player)) return;
        //Let's do some actions
        scriptActions.runScripts(eliteEntity, player);
        //Cooldowns time
        doCooldownTicks(eliteEntity);
    }

    /**
     * Used by scripts that call other scripts as the trigger
     *
     * @param eliteEntity
     * @param directTarget
     */
    public void check(EliteEntity eliteEntity, LivingEntity directTarget) {
        //If the script uses the cooldown system then it should respect if the boss is in a global or local cooldown state
        if (scriptCooldowns != null && super.isInCooldown(eliteEntity)) return;
        //Check if the event conditions are met
        if (scriptConditions != null && !scriptConditions.meetsConditions(eliteEntity, directTarget)) return;
        //Let's do some actions
        scriptActions.runScripts(eliteEntity, directTarget);
        //Cooldowns time
        doCooldownTicks(eliteEntity);
    }

}
