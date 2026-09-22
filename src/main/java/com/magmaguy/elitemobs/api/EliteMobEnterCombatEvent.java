package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class EliteMobEnterCombatEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player targetEntity;
    private final EliteEntity eliteEntity;

    public EliteMobEnterCombatEvent(EliteEntity eliteEntity, Player targetEntity) {
        this.targetEntity = targetEntity;
        this.eliteEntity = eliteEntity;

        if (!DefaultConfig.isAlwaysShowNametags())
            eliteEntity.setNameVisible(true);
        // Phase bosses may publish another enter event while the encounter is already active.
        if (!eliteEntity.isInCombat()) eliteEntity.startCombatWatchdog();
        eliteEntity.setInCombat(true);
        if (eliteEntity instanceof CustomBossEntity customBossEntity)
            CommandRunner.runCommandFromList(customBossEntity.getCustomBossesConfigFields().getOnCombatEnterCommands(), new ArrayList<>());
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getTargetEntity() {
        return this.targetEntity;
    }

    public EliteEntity getEliteMobEntity() {
        return this.eliteEntity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class EliteMobEnterCombatEventFilter implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onEliteMobDamaged(EliteMobDamagedByPlayerEvent event) {
            if (event.getEliteMobEntity().isInCombat()) return;
            if (!(event.getEliteMobEntity().getLivingEntity() instanceof Mob)) return;
            Bukkit.getServer().getPluginManager().callEvent(new EliteMobEnterCombatEvent(event.getEliteMobEntity(), event.getPlayer()));
        }

        @EventHandler(ignoreCancelled = true)
        public void onEliteMobDamage(PlayerDamagedByEliteMobEvent event) {
            if (event.getEliteMobEntity().isInCombat()) return;
            if (!(event.getEliteMobEntity().getLivingEntity() instanceof Mob)) return;
            Bukkit.getServer().getPluginManager().callEvent(new EliteMobEnterCombatEvent(event.getEliteMobEntity(), event.getPlayer()));
        }

        @EventHandler(ignoreCancelled = true)
        public void onEliteMobTarget(EliteMobTargetPlayerEvent event) {
            if (event.getEliteMobEntity().isInCombat()) return;
            Bukkit.getServer().getPluginManager().callEvent(new EliteMobEnterCombatEvent(event.getEliteMobEntity(), event.getPlayer()));
        }
    }
}
