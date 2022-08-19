package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.Taunt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CustomBossTaunts implements Listener {
    @EventHandler
    public void onDamagedMessages(EliteMobDamagedEvent eliteMobDamagedEvent) {
        if (!(eliteMobDamagedEvent.getEliteMobEntity() instanceof CustomBossEntity)) return;
        if (((CustomBossEntity) eliteMobDamagedEvent.getEliteMobEntity()).getCustomBossesConfigFields().getOnDamagedMessages() == null)
            return;
        Taunt.nameTagProcessor(eliteMobDamagedEvent.getEliteMobEntity(), eliteMobDamagedEvent.getEliteMobEntity().getLivingEntity(),
                ((CustomBossEntity) eliteMobDamagedEvent.getEliteMobEntity()).getCustomBossesConfigFields().getOnDamagedMessages());
    }

    @EventHandler
    public void onDamageMessages(PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
        if (!(playerDamagedByEliteMobEvent.getEliteMobEntity() instanceof CustomBossEntity)) return;
        if (((CustomBossEntity) playerDamagedByEliteMobEvent.getEliteMobEntity()).getCustomBossesConfigFields().getOnDamageMessages()== null)
            return;
        Taunt.nameTagProcessor(playerDamagedByEliteMobEvent.getEliteMobEntity(), playerDamagedByEliteMobEvent.getEliteMobEntity().getLivingEntity(),
                ((CustomBossEntity) playerDamagedByEliteMobEvent.getEliteMobEntity()).getCustomBossesConfigFields().getOnDamageMessages());
    }
}
