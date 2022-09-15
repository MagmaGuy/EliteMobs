package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class PillagerCaravanBeastBoss extends CustomBossesConfigFields {
    public PillagerCaravanBeastBoss() {
        super("pillager_caravan_beast",
                EntityType.RAVAGER,
                true,
                "$reinforcementLevel &cCaravan Beast",
                "dynamic");
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setFollowDistance(100);
        setPowers(List.of("attack_confusing.yml"));
    }
}
