package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.AttributeManager;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.EntitySearch;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
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
        if (eliteEntity instanceof CustomBossEntity customBossEntity)
            CommandRunner.runCommandFromList(customBossEntity.getCustomBossesConfigFields().getOnCombatEnterCommands(), new ArrayList<>());
        //Phase bosses can launch this event through phase switches
        if (!eliteEntity.isInCombat())
            Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN, task -> {
                if (!eliteEntity.isValid()) {
                    task.cancel();
                    Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, syncTask -> new EventCaller(new EliteMobExitCombatEvent(eliteEntity, EliteMobExitCombatEvent.EliteMobExitCombatReason.ELITE_NOT_VALID)));
                    return;
                }
                if (!eliteEntity.isInCombatGracePeriod()) {
                    double followRange = AttributeManager.getAttributeBaseValue(eliteEntity.getLivingEntity(), "generic_follow_range");
                    if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                        followRange = 200;
                    if (EntitySearch.getNearbyCombatPlayers(eliteEntity.getLocation(), followRange).isEmpty()) {
                        task.cancel();
                        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, syncTask -> new EventCaller(new EliteMobExitCombatEvent(eliteEntity, EliteMobExitCombatEvent.EliteMobExitCombatReason.NO_NEARBY_PLAYERS)));
                    }
                }
            }, 20L, 20L);
        eliteEntity.setInCombat(true);
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
