package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

/**
 * Premade damage test dummy for combat validation tests.
 * <p>
 * Always available regardless of installed content packages (unlike YAML-based
 * training dummies which depend on the adventurer's guild content).
 * <p>
 * Configuration:
 * <ul>
 *   <li>Level 50 — mid-range for balanced baseline testing</li>
 *   <li>damageMultiplier=1.0 — deals normal damage (needed for defensive tests)</li>
 *   <li>healthMultiplier=1000.0 — extremely tanky so that even max-level single hits
 *       (~228K damage at Lv.100 skill vs Lv.50 mob) never exceed the dummy's HP
 *       (2.24M at Lv.50), keeping health-delta damage measurements accurate</li>
 *   <li>No AI, no loot, no XP drops — pure test entity</li>
 * </ul>
 */
public class DamageTestDummyConfig extends CustomBossesConfigFields {
    public DamageTestDummyConfig() {
        super("damage_test_dummy", EntityType.HUSK, true,
                "&8[&6Lv.50 &7Damage Test Dummy&8]", "50");
        setDamageMultiplier(1.0);
        setHealthMultiplier(1000.0);
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setDropsRandomLoot(false);
        setDropsSkillXP(false);
        setSilent(true);
        setAi(false);
    }
}
