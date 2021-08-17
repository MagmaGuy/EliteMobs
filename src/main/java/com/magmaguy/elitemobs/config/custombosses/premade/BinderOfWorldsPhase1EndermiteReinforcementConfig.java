package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class BinderOfWorldsPhase1EndermiteReinforcementConfig extends CustomBossesConfigFields {
    public BinderOfWorldsPhase1EndermiteReinforcementConfig() {
        super("binder_of_worlds_phase_1_endermite_reinforcement",
                EntityType.ENDERMITE,
                true,
                "$reinforcementLevel &5Reality Corruptor",
                "200");
        setTimeout(2);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
        setHealthMultiplier(0.1);
        setDamageMultiplier(0.5);
    }
}
