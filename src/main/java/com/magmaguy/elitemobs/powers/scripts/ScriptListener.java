package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ScriptListener implements Listener {
    @EventHandler
    public void onEliteMobDamagedByEliteMobEvent(EliteMobDamagedByEliteMobEvent event) {
        runEvent(event, event.getDamager(), null);
    }

    @EventHandler
    public void onEliteMobDamagedByPlayerEvent(EliteMobDamagedByPlayerEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getPlayer());
    }

    @EventHandler
    public void onEliteMobDamagedEvent(EliteMobDamagedEvent event) {
        runEvent(event, event.getEliteMobEntity(), null);
    }

    @EventHandler
    public void onEliteMobDeathEvent(EliteMobDeathEvent event) {
        runEvent(event, event.getEliteEntity(), null);
    }

    @EventHandler
    public void onEliteMobsEnterCombatEvent(EliteMobEnterCombatEvent event) {
        runEvent(event, event.getEliteMobEntity(), event.getTargetEntity());
    }

    @EventHandler
    public void onEliteMobExitCombatEvent(EliteMobExitCombatEvent event) {
        runEvent(event, event.getEliteMobEntity(), null);
    }

    @EventHandler
    public void onEliteMobHealEvent(EliteMobHealEvent event) {
        runEvent(event, event.getEliteEntity(), null);
    }

    @EventHandler
    public void onEliteMobSpawnEvent(EliteMobSpawnEvent event) {
        runEvent(event, event.getEliteMobEntity(), null);
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
    public void onElitePhaseSwitchEvent(ElitePhaseSwitchEvent event){
        runEvent(event,event.getCustomBossEntity(), null);
    }

    private void runEvent(Event event, EliteEntity eliteEntity, Player player) {
        for (ElitePower elitePower : eliteEntity.getElitePowers()) {
            if (elitePower instanceof EliteScript) {
                ((EliteScript) elitePower).check(event.getClass(), eliteEntity, player);
            }
        }
    }
}
