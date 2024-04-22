package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.HashMap;

public class ScriptListener implements Listener {
    public static HashMap<FallingBlock, FallingEntityDataPair> fallingBlocks = new HashMap();
    public static HashMap<Entity, FallingEntityDataPair> fallingEntities = new HashMap<>();

    @EventHandler
    public void onEliteMobDamagedByPlayerEvent(EliteMobDamagedByPlayerEvent event) {
        if (event.isCancelled()) return;
        runEvent(event, event.getEliteMobEntity(), event.getPlayer());
    }

    @EventHandler
    public void onEliteMobDamagedByEliteMobEvent(EliteMobDamagedByEliteMobEvent event) {
        if (event.isCancelled()) return;
        runEvent(event, event.getDamager());
    }

    @EventHandler
    public void onEliteMobDamagedEvent(EliteMobDamagedEvent event) {
        if (event.isCancelled()) return;
        runEvent(event, event.getEliteEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobsEnterCombatEvent(EliteMobEnterCombatEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getTargetEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobDeathEvent(EliteMobDeathEvent event) {
        runEvent(event, event.getEliteEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobExitCombatEvent(EliteMobExitCombatEvent event) {
        runEvent(event, event.getEliteMobEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobHealEvent(EliteMobHealEvent event) {
        if (event.isCancelled()) return;
        runEvent(event, event.getEliteEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobTargetPlayerEvent(EliteMobTargetPlayerEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getPlayer());
    }

    @EventHandler
    public void onPlayerDamagedByEliteMobEvent(PlayerDamagedByEliteMobEvent event) {
        if (event.isCancelled()) return;
        runEvent(event, event.getEliteMobEntity(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobSpawnEvent(EliteMobSpawnEvent event) {
        if (event.isCancelled()) return;
        runEvent(event, event.getEliteMobEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onElitePhaseSwitchEvent(ElitePhaseSwitchEvent event) {
        runEvent(event, event.getCustomBossEntity());
    }

    //This is use to track falling blocks
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (fallingBlocks.isEmpty()) return;
        FallingEntityDataPair fallingEntityDataPair = fallingBlocks.get(event.getEntity());
        if (fallingEntityDataPair == null) return;
        ScriptAction scriptAction = fallingEntityDataPair.getScriptAction();
        if (scriptAction == null) return;
        event.setCancelled(true);
        runEvent(fallingBlocks.get(event.getEntity()), event.getBlock().getLocation());
        fallingBlocks.remove(event.getEntity());
    }

    private void runEvent(Event event, EliteEntity eliteEntity) {
        for (ElitePower elitePower : eliteEntity.getElitePowers())
            if (elitePower instanceof EliteScript eliteScript)
                eliteScript.check(event, eliteEntity, null);
    }

    private void runEvent(Event event, EliteEntity eliteEntity, Player player) {
        for (ElitePower elitePower : eliteEntity.getElitePowers())
            if (elitePower instanceof EliteScript eliteScript)
                eliteScript.check(event, eliteEntity, player);
    }

    public static void runEvent(FallingEntityDataPair fallingEntityDataPair, Location landingLocation) {
        for (String string : fallingEntityDataPair.getScriptAction().getBlueprint().getLandingScripts()) {
            EliteScript iteratedScript = fallingEntityDataPair.getScriptAction().getEliteScriptMap().get(string);
            if (iteratedScript == null) {
                new WarningMessage("Elite script " + string + " does not exist for landing scripts!");
                return;
            }
            iteratedScript.check(landingLocation, fallingEntityDataPair.getScriptActionData());
        }
    }
}
