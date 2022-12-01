package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EliteScript extends ElitePower implements Cloneable {
    protected final ScriptActions scriptActions;
    //Parse from power file
    private final ScriptEvents scriptEvents;
    @Getter
    private final ScriptZone scriptZone;
    private final ScriptCooldowns scriptCooldowns;
    @Getter
    protected Map<String, EliteScript> eliteScriptMap;
    private ScriptConditions scriptConditions;

    public EliteScript(EliteScriptBlueprint scriptBlueprint, Map<String, EliteScript> eliteScriptMap) {
        super(scriptBlueprint.getCustomConfigFields());
        this.eliteScriptMap = eliteScriptMap;
        this.scriptEvents = new ScriptEvents(scriptBlueprint.getScriptEventsBlueprint());
        this.scriptConditions = new ScriptConditions(scriptBlueprint.getScriptConditionsBlueprint(), this);
        this.scriptZone = new ScriptZone(scriptBlueprint.getScriptZoneBlueprint(), this);
        this.scriptActions = new ScriptActions(scriptBlueprint.getScriptActionsBlueprint(), eliteScriptMap, this);
        this.scriptCooldowns = new ScriptCooldowns(scriptBlueprint.getScriptCooldownsBlueprint(), this);
        eliteScriptMap.put(scriptBlueprint.getScriptName(), this);
    }

    //Parse from boss config
    public static List<EliteScript> generateBossScripts(List<EliteScriptBlueprint> blueprints) {
        //The map is declared here because it needs to be shared inside of all scripts in the same file so they can be referenced.
        HashMap<String, EliteScript> powerMap = new HashMap();
        return blueprints.stream().map(eliteScriptBlueprint -> new EliteScript(eliteScriptBlueprint, powerMap)).collect(Collectors.toList());
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
        //If the script does not define a local or global cooldown then it is considered to ignore cooldowns. This is an
        //important bypass for a lot of behavior like teleporting at specific triggers regardless of state
        if (getPowerCooldownTime() > 0 && getGlobalCooldownTime() > 0 &&
                scriptCooldowns != null && super.isInCooldown(eliteEntity)) return;
        //Check if the event is relevant to the script
        if (!scriptEvents.isTargetEvent(eventClass)) return;
        //Check if the event conditions are met
        if (scriptConditions != null && !scriptConditions.meetsConditionsOutsideOfAction(eliteEntity, player)) return;
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
        //Check if the event conditions are met
        if (scriptConditions != null && !scriptConditions.meetsConditionsOutsideOfAction(eliteEntity, directTarget)) return;
        //Let's do some actions
        scriptActions.runScripts(eliteEntity, directTarget);
        //Cooldowns time
        doCooldownTicks(eliteEntity);
    }

    /**
     * Used by scripts that call specific scripts when a projectile or falling block lands
     *
     * @param eliteEntity     Boss
     * @param landingLocation Location where the projectile or block landed
     */
    public void check(EliteEntity eliteEntity, Location landingLocation) {
        //Check if the event conditions are met
        if (scriptConditions != null && !scriptConditions.meetsConditionsOutsideOfAction(eliteEntity, null)) return;
        //Let's do some actions
        scriptActions.runScripts(eliteEntity, landingLocation);
        //Cooldowns time
        doCooldownTicks(eliteEntity);
    }

}
