package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the Debt Collector boss that spawns for players in gambling debt.
 * This intimidating boss hunts down players who owe the casino money.
 */
public class DebtCollectorConfig extends CustomBossesConfigFields {

    public DebtCollectorConfig() {
        super("debt_collector",
                EntityType.VINDICATOR,
                true,
                "$bossLevel <g:#8B0000:#DC143C>The Debt Collector</g>",
                "dynamic");

        // Health and damage settings from config
        setHealthMultiplier(5.0);
        setDamageMultiplier(1.0);

        // Don't persist - timeout or kill removes
        setPersistent(false);

        // Timeout is handled by DebtCollectorManager, but set a fallback
        setTimeout(600); // 10 minutes

        // Powers - mix of threatening abilities
        setPowers(new ArrayList<>(List.of(
                "attack_push.yml",           // Knockback attacks
                "attack_blinding.yml",       // Blind the target briefly
                "movement_speed.yml",        // Fast pursuer
                "attack_gravity.yml",        // Pull player back if they try to run
                "spirit_walk.yml"            // Phase through blocks to chase
        )));

        // Smoke trails for menacing appearance
        setTrails(new ArrayList<>(List.of(
                Particle.LARGE_SMOKE.toString(),
                Particle.SOUL.toString(),
                Particle.LARGE_SMOKE.toString()
        )));

        // No loot drops - this is a consequence, not a reward
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setDropsRandomLoot(false);
        setDropsSkillXP(false);

        // Messages are handled programmatically in DebtCollectorManager
        // but we can set some combat messages here
        setOnDamageMessages(new ArrayList<>(List.of(
                "&c[Debt Collector] &7Consider that... &einterest&7.",
                "&c[Debt Collector] &7Every hit is a &6payment&7.",
                "&c[Debt Collector] &7You can't escape your debts!"
        )));

        setOnDamagedMessages(new ArrayList<>(List.of(
                "&c[Debt Collector] &7That all you got?",
                "&c[Debt Collector] &7You'll pay for that... literally.",
                "&c[Debt Collector] &7Fighting back won't clear your debt!"
        )));
    }
}
