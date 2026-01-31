package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Training Dummy - Level 100
 * A passive, stationary target for testing combat and skills.
 * Uses HUSK entity type (humanoid mob that can be hit by projectiles).
 */
public class TrainingDummyLv100Config extends CustomBossesConfigFields {
    public TrainingDummyLv100Config() {
        super("training_dummy_lv100",
                EntityType.HUSK,
                true,
                "&8[&5Lv.100 &7Training Dummy&8]",
                "100");
        setRegionalBoss(true);
        setSpawnCooldown(0);
        setHealthMultiplier(1.0);
        setDamageMultiplier(0.0);
        setSilent(true);
        setAi(false);
        setAlwaysShowName(true);
        setFollowDistance(15);
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setDropsRandomLoot(false);
        setDropsSkillXP(false);
        setCustomModel("em_trainingdummy5");
        setSpawnLocations(List.of("em_adventurers_guild,240.5,87,205.5,-17,0"));
    }
}
