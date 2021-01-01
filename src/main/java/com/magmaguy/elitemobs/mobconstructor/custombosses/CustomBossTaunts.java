package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.Taunt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CustomBossTaunts implements Listener {
    @EventHandler
    public void onDamagedMessages(EliteMobDamagedEvent eliteMobDamagedEvent) {
        if (eliteMobDamagedEvent.getEliteMobEntity().customBossEntity == null) return;
        if (eliteMobDamagedEvent.getEliteMobEntity().customBossEntity.customBossConfigFields.getOnDamagedMessages().isEmpty())
            return;
        Taunt.nametagProcessor(eliteMobDamagedEvent.getEliteMobEntity().getLivingEntity(),
                eliteMobDamagedEvent.getEliteMobEntity().customBossEntity.customBossConfigFields.getOnDamagedMessages());
    }

    @EventHandler
    public void onDamageMessages(PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
        if (playerDamagedByEliteMobEvent.getEliteMobEntity().customBossEntity == null) return;
        if (playerDamagedByEliteMobEvent.getEliteMobEntity().customBossEntity.customBossConfigFields.getOnDamageMessages().isEmpty())
            return;
        Taunt.nametagProcessor(playerDamagedByEliteMobEvent.getEliteMobEntity().customBossEntity.getLivingEntity(),
                playerDamagedByEliteMobEvent.getEliteMobEntity().customBossEntity.customBossConfigFields.getOnDamageMessages());
    }
}
