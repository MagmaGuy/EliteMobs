package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class BinderOfWorldsPhase3GhastReinforcementConfig extends CustomBossesConfigFields {
    public BinderOfWorldsPhase3GhastReinforcementConfig() {
        super("binder_of_worlds_phase_3_ghast_reinforcement",
                EntityType.GHAST,
                true,
                "$reinforcementLevel &5Reality Shredder",
                "200");
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
        setTimeout(2);
        setFollowDistance(200);
    }
}
