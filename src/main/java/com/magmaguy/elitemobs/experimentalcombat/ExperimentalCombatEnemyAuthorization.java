package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.experimentalcombat.minions.ClassMinionIdentity;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

/** Canonical legality boundary for hostile Experimental Combat target acquisition. */
public final class ExperimentalCombatEnemyAuthorization {
    private ExperimentalCombatEnemyAuthorization() {
    }

    public static boolean canTarget(Player caster, LivingEntity candidate) {
        return ExperimentalCombatEnemyPolicy.permits(targetFacts(caster, candidate));
    }

    /** Broader magic-weapon rule: any living hostile, passive mob or player, not only elites. */
    public static boolean canTargetWithMagicWeapon(Player caster, LivingEntity candidate) {
        return ExperimentalCombatEnemyPolicy.permitsMagicWeapon(targetFacts(caster, candidate));
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts targetFacts(
            Player caster, LivingEntity candidate) {
        if (caster == null || candidate == null)
            return new ExperimentalCombatEnemyPolicy.TargetFacts(
                    false, false, true, true, true, false, false);

        boolean casterReady = caster.isOnline()
                && caster.isValid()
                && !caster.isDead();
        boolean candidateReady = candidate.isValid()
                && !candidate.isDead()
                && !candidate.isInvulnerable()
                && caster.getWorld().equals(candidate.getWorld());
        boolean npc = candidate.hasMetadata("NPC") || EntityTracker.isNPCEntity(candidate);
        boolean friendly = candidate instanceof Tameable tameable && tameable.isTamed();
        boolean classMinion = ClassMinionIdentity.isMinion(candidate);

        EliteEntity elite = EntityTracker.getEliteMobEntity(candidate);
        boolean validElite = elite != null && elite.isValid() && !elite.isDying();
        if (candidate instanceof EnderDragon dragon && dragon.getPhase() == EnderDragon.Phase.DYING)
            validElite = false;

        boolean instanceAuthorized = !(PlayerData.getMatchInstance(caster) instanceof DungeonInstance instance)
                || instance.authorizesCombatTarget(caster, candidate.getLocation());
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                casterReady,
                candidateReady,
                npc,
                friendly,
                classMinion,
                validElite,
                instanceAuthorized);
    }
}
