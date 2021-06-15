package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class BinderOfWorldsPhase3GhastReinforcementConfig extends CustomBossConfigFields {
    public BinderOfWorldsPhase3GhastReinforcementConfig() {
        super("binder_of_worlds_phase_3_ghast_reinforcement",
                EntityType.GHAST.toString(),
                true,
                "$reinforcementLevel &5Reality Shredder",
                "250");
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
        setTimeout(2);
        setFollowDistance(200);
    }
}
