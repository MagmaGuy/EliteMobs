package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;

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
        setSpawnCooldown(1); // 1 minute respawn
        setHealthMultiplier(1.0);
        setDamageMultiplier(0.0); // Doesn't deal damage
        setNeutral(true);
        setFrozen(true);
        setSilent(true);
        setLeashRadius(1);
        setFollowDistance(0);
        setTimeout(0);
        setPersistent(true);
        setRemoveAfterDeath(false);
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setDropsRandomLoot(false);
        setDropsSkillXP(false);
        setPowers(new ArrayList<>());
        setAnnouncementPriority(0);
    }
}
