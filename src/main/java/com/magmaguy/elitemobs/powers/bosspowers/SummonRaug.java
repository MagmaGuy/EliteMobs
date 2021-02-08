package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.BossPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SummonRaug extends BossPower implements Listener {

    public SummonRaug() {
        super(PowersConfig.getPower("summon_raug.yml"));
    }

    @EventHandler
    public void onEliteMobDamage(EliteMobDamagedByPlayerEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEliteMobEntity().hasPower(this)) return;
        if (event.getEntityDamageByEntityEvent().getFinalDamage() < 2) return;
        CustomBossEntity.constructCustomBoss("raug.yml", event.getEntity().getLocation(), event.getEliteMobEntity().getLevel());
        CustomBossEntity.constructCustomBoss("raug.yml", event.getEntity().getLocation(), event.getEliteMobEntity().getLevel());
    }

}
