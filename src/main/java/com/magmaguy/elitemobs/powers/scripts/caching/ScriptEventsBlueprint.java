package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScriptEventsBlueprint {
    @Getter
    private final Set<Class> events = new HashSet<>();
    @Getter
    private boolean zoneListener = false;

    public ScriptEventsBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        List<String> values = configurationSection.getStringList("Events");
        for (String entry : values) {
            switch (entry) {
                case "EliteMobDamagedByEliteMobEvent" -> events.add(EliteMobDamagedByEliteMobEvent.class);
                case "EliteMobDamagedByPlayerEvent" -> events.add(EliteMobDamagedByPlayerEvent.class);
                case "EliteMobDamagedEvent" -> events.add(EliteMobDamagedEvent.class);
                case "EliteMobDeathEvent" -> events.add(EliteMobDeathEvent.class);
                case "EliteMobEnterCombatEvent" -> events.add(EliteMobEnterCombatEvent.class);
                case "EliteMobExitCombatEvent" -> events.add(EliteMobExitCombatEvent.class);
                case "EliteMobHealEvent" -> events.add(EliteMobHealEvent.class);
                case "EliteMobSpawnEvent" -> events.add(EliteMobSpawnEvent.class);
                case "EliteMobTargetPlayerEvent" -> events.add(EliteMobTargetPlayerEvent.class);
                case "PlayerDamagedByEliteMobEvent" -> events.add(PlayerDamagedByEliteMobEvent.class);
                case "ElitePhaseSwitchEvent" -> events.add(ElitePhaseSwitchEvent.class);
                case "ZoneEnterEvent" -> {
                    events.add(ScriptZoneEnterEvent.class);
                    zoneListener = true;
                }
                case "ZoneLeaveEvent" -> {
                    events.add(ScriptZoneLeaveEvent.class);
                    zoneListener = true;
                }
                default ->
                        Logger.warn("Failed to get valid script event from entry " + entry + " in " + scriptName + " for file " + filename + " !");
            }
        }
    }
}
