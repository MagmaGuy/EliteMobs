package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class EliteMobEnterCombatEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player targetEntity;
    private final EliteMobEntity eliteMobEntity;

    public EliteMobEnterCombatEvent(EliteMobEntity eliteMobEntity, Player targetEntity) {
        this.targetEntity = targetEntity;
        this.eliteMobEntity = eliteMobEntity;
        eliteMobEntity.setIsInCombat(true);
        if (!DefaultConfig.alwaysShowNametags)
            eliteMobEntity.setNameVisible(true);
        if (eliteMobEntity instanceof CustomBossEntity)
            CommandRunner.runCommandFromList(((CustomBossEntity) eliteMobEntity).customBossConfigFields.getOnCombatEnterCommands(), new ArrayList<>());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!eliteMobEntity.getLivingEntity().isValid()) {
                    cancel();
                    Bukkit.getServer().getPluginManager().callEvent(new EliteMobExitCombatEvent(eliteMobEntity));
                }
                //todo: combat isn't ending when no players are nearby
                if (!eliteMobEntity.isInCombatGracePeriod())
                    if (((Mob) eliteMobEntity.getLivingEntity()).getTarget() == null) {
                        for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(
                                eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue(),
                                eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue(),
                                eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue())) {
                            if (entity instanceof Player) {
                                if (!(((Player) entity).getGameMode().equals(GameMode.SURVIVAL) || ((Player) entity).getGameMode().equals(GameMode.ADVENTURE)))
                                    continue;
                                return;
                            }
                        }
                        cancel();
                        Bukkit.getServer().getPluginManager().callEvent(new EliteMobExitCombatEvent(eliteMobEntity));
                    }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);
    }

    public Player getTargetEntity() {
        return this.targetEntity;
    }

    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static class EliteMobEnterCombatEventFilter implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onEliteMobDamaged(EliteMobDamagedByPlayerEvent event) {
            if (event.getEliteMobEntity().isInCombat()) return;
            if (!(event.getEliteMobEntity().getLivingEntity() instanceof Mob)) return;
            if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || event.getPlayer().getGameMode().equals(GameMode.ADVENTURE))
                Bukkit.getServer().getPluginManager().callEvent(new EliteMobEnterCombatEvent(event.getEliteMobEntity(), event.getPlayer()));
        }

        @EventHandler(ignoreCancelled = true)
        public void onEliteMobDamage(PlayerDamagedByEliteMobEvent event) {
            if (event.getEliteMobEntity().isInCombat()) return;
            if (!(event.getEliteMobEntity().getLivingEntity() instanceof Mob)) return;
            if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || event.getPlayer().getGameMode().equals(GameMode.ADVENTURE))
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
