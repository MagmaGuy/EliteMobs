package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class BinderOfWorldsPhase1EndermiteReinforcementConfig extends CustomBossConfigFields {
    public BinderOfWorldsPhase1EndermiteReinforcementConfig() {
        super("binder_of_worlds_phase_1_endermite_reinforcement",
                EntityType.ENDERMITE.toString(),
                true,
                "$reinforcementLevel &5Reality Corruptor",
                "250");
        setTimeout(2);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
        setHealthMultiplier(0.1);
        setDamageMultiplier(0.5);
    }
}
