package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class PillagerCaravanGuardBoss extends CustomBossesConfigFields {
    public PillagerCaravanGuardBoss() {
        super("pillager_caravan_guard",
                EntityType.PILLAGER,
                true,
                "$reinforcementLevel &cCaravan Guard",
                "dynamic");
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setMountedEntity("pillager_caravan_beast.yml");
        setFollowDistance(100);
        setPowers(new ArrayList<>(List.of("attack_poison.yml")));
    }
}
