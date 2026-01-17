package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Training Dummy - Level 1
 * A passive, stationary target for testing combat and skills.
 * Uses HUSK entity type (humanoid mob that can be hit by projectiles).
 */
public class TrainingDummyLv1Config extends CustomBossesConfigFields {
    public TrainingDummyLv1Config() {
        super("training_dummy_lv1",
                EntityType.HUSK,
                true,
                "&8[&aLv.1 &7Training Dummy&8]",
                "1");
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
        setCustomModel("em_trainingdummy1");
        setSpawnLocations(List.of("em_adventurers_guild,229.5,87,216.5,-70,0"));
    }
}
