package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class EliteMobExitCombatEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final EliteEntity eliteEntity;
    private final EliteMobExitCombatReason eliteMobExitCombatReason;


    public EliteMobExitCombatEvent(EliteEntity eliteEntity, EliteMobExitCombatReason reason) {
        this.eliteEntity = eliteEntity;
        this.eliteMobExitCombatReason = reason;
        eliteEntity.setInCombat(false);
        if (eliteEntity.getUnsyncedLivingEntity().isDead()) return;
        //only run commands if the reason for leaving combat isn't death, onDeath commands exist for that case
        if (eliteEntity instanceof CustomBossEntity)
            CommandRunner.runCommandFromList(((CustomBossEntity) eliteEntity).getCustomBossesConfigFields().getOnCombatLeaveCommands(), new ArrayList<>());
        if (eliteEntity.isValid() &&
                MobCombatSettingsConfig.regenerateCustomBossHealthOnCombatEnd &&
                !reason.equals(EliteMobExitCombatReason.PHASE_SWITCH) &&
                !eliteEntity.getUnsyncedLivingEntity().getType().equals(EntityType.PHANTOM))
            eliteEntity.fullHeal();
        if (!DefaultConfig.alwaysShowNametags)
            eliteEntity.setNameVisible(false);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public EliteEntity getEliteMobEntity() {
        return this.eliteEntity;
    }

    public EliteMobExitCombatReason getEliteMobExitCombatReason() {
        return eliteMobExitCombatReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum EliteMobExitCombatReason {
        NO_NEARBY_PLAYERS,
        SPIRIT_WALK,
        ELITE_NOT_VALID,
        PHASE_SWITCH
    }

}
