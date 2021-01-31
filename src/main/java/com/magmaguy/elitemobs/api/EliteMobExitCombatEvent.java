package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class EliteMobExitCombatEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final EliteMobEntity eliteMobEntity;

    public EliteMobExitCombatEvent(EliteMobEntity eliteMobEntity) {
        this.eliteMobEntity = eliteMobEntity;
        eliteMobEntity.setIsInCombat(false);
        if (eliteMobEntity.getLivingEntity().isDead()) return;
        //only run commands if the reason for leaving combat isn't death, onDeath commands exist for that case
        if (eliteMobEntity instanceof CustomBossEntity)
            CommandRunner.runCommandFromList(((CustomBossEntity) eliteMobEntity).customBossConfigFields.getOnCombatLeaveCommands(), new ArrayList<>());
        if (MobCombatSettingsConfig.regenerateCustomBossHealthOnCombatEnd)
            if (!eliteMobEntity.getLivingEntity().getType().equals(EntityType.PHANTOM))
                eliteMobEntity.fullHeal();
        if (!DefaultConfig.alwaysShowNametags)
            eliteMobEntity.setNameVisible(false);
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

}
