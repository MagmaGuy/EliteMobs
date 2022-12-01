package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.HashMap;

public class ScriptListener implements Listener {
    public static HashMap<FallingBlock, ScriptAction> fallingBlocks = new HashMap();

    @EventHandler
    public void onEliteMobDamagedByPlayerEvent(EliteMobDamagedByPlayerEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getPlayer());
    }

    @EventHandler
    public void onEliteMobDamagedByEliteMobEvent(EliteMobDamagedByEliteMobEvent event) {
        runEvent(event, event.getDamager());
    }

    @EventHandler
    public void onEliteMobDamagedEvent(EliteMobDamagedEvent event) {
        runEvent(event, event.getEliteEntity());
    }

    @EventHandler
    public void onEliteMobsEnterCombatEvent(EliteMobEnterCombatEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getTargetEntity());
    }

    @EventHandler
    public void onEliteMobDeathEvent(EliteMobDeathEvent event) {
        runEvent(event, event.getEliteEntity());
    }

    @EventHandler
    public void onEliteMobExitCombatEvent(EliteMobExitCombatEvent event) {
        runEvent(event, event.getEliteMobEntity());
    }

    @EventHandler
    public void onEliteMobHealEvent(EliteMobHealEvent event) {
        runEvent(event, event.getEliteEntity());
    }

    @EventHandler
    public void onEliteMobTargetPlayerEvent(EliteMobTargetPlayerEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getPlayer());
    }

    @EventHandler
    public void onPlayerDamagedByEliteMobEvent(PlayerDamagedByEliteMobEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getPlayer());
    }

    @EventHandler
    public void onEliteMobSpawnEvent(EliteMobSpawnEvent event) {
        runEvent(event, event.getEliteMobEntity());
    }

    @EventHandler
    public void onElitePhaseSwitchEvent(ElitePhaseSwitchEvent event) {
        runEvent(event, event.getCustomBossEntity());
    }

    //This is use to track falling blocks
    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (fallingBlocks.isEmpty()) return;
        ScriptAction scriptAction = fallingBlocks.get(event.getEntity());
        if (scriptAction == null) return;
        event.setCancelled(true);
        runEvent(scriptAction, event.getBlock().getLocation());
    }

    private void runEvent(Event event, EliteEntity eliteEntity) {
        for (ElitePower elitePower : eliteEntity.getElitePowers())
            if (elitePower instanceof EliteScript eliteScript)
                eliteScript.check(event.getClass(), eliteEntity, null);
    }

    private void runEvent(Event event, EliteEntity eliteEntity, Player player) {
        for (ElitePower elitePower : eliteEntity.getElitePowers())
            if (elitePower instanceof EliteScript eliteScript)
                eliteScript.check(event.getClass(), eliteEntity, player);
    }

    private void runEvent(ScriptAction scriptAction, Location landingLocation) {
        for (String string : scriptAction.getBlueprint().getLandingScripts()) {
            EliteScript iteratedScript = scriptAction.getEliteScriptMap().get(string);
            if (iteratedScript == null) {
                new WarningMessage("Elite script " + string + " does not exist for landing scripts!");
                return;
            }
            iteratedScript.check(scriptAction.getEliteEntity(), landingLocation);
        }
    }
}
