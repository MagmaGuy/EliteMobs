package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class BinderOfWorldsPhase3EndermanReinforcementConfig extends CustomBossesConfigFields {
    public BinderOfWorldsPhase3EndermanReinforcementConfig() {
        super("binder_of_worlds_phase_3_enderman_reinforcement",
                EntityType.ENDERMAN,
                true,
                "$reinforcementLevel &5Reality Slayer",
                "250");
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(true);
        setFollowDistance(200);
        setDamageMultiplier(2);
        setHealthMultiplier(5);
    }
}
