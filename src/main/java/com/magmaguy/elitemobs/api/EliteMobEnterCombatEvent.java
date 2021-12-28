package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
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
        eliteEntity.setInCombat(true);
        if (!DefaultConfig.isAlwaysShowNametags())
            eliteEntity.setNameVisible(true);
        if (eliteEntity instanceof CustomBossEntity)
            CommandRunner.runCommandFromList(((CustomBossEntity) eliteEntity).getCustomBossesConfigFields().getOnCombatEnterCommands(), new ArrayList<>());
        Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, (task) -> {
            if (!eliteEntity.isValid()) {
                task.cancel();
                new EventCaller(new EliteMobExitCombatEvent(eliteEntity, EliteMobExitCombatEvent.EliteMobExitCombatReason.ELITE_NOT_VALID));
                return;
            }
            if (!eliteEntity.isInCombatGracePeriod())
                if (((Mob) eliteEntity.getLivingEntity()).getTarget() == null) {
                    for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(
                            eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue(),
                            eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue(),
                            eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue())) {
                        if (entity instanceof Player) {
                            if (((Player) entity).getGameMode().equals(GameMode.SPECTATOR))
                                continue;
                            return;
                        }
                    }
                    task.cancel();
                    new EventCaller(new EliteMobExitCombatEvent(eliteEntity, EliteMobExitCombatEvent.EliteMobExitCombatReason.NO_NEARBY_PLAYERS));
                }
        }, 20L, 20L);
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
            if (!(event.getEliteMobEntity().getLivingEntity() instanceof Mob)) return;
            Bukkit.getServer().getPluginManager().callEvent(new EliteMobEnterCombatEvent(event.getEliteMobEntity(), event.getPlayer()));
        }
    }
}
