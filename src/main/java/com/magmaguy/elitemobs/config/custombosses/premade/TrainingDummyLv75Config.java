package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Training Dummy - Level 75
 * A passive, stationary target for testing combat and skills.
 * Uses HUSK entity type (humanoid mob that can be hit by projectiles).
 */
public class TrainingDummyLv75Config extends CustomBossesConfigFields {
    public TrainingDummyLv75Config() {
        super("training_dummy_lv75",
                EntityType.HUSK,
                true,
                "&8[&cLv.75 &7Training Dummy&8]",
                "75");
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
        setCustomModel("em_trainingdummy4");
        setSpawnLocations(List.of("em_adventurers_guild,236.5,87,206.5,-31,0"));
    }
}
