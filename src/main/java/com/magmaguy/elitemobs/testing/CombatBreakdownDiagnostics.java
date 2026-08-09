package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** Owns the optional detailed damage-breakdown diagnostic workflow. */
final class CombatBreakdownDiagnostics {

    private final CombatSimulator simulator;
    private final Player player;

    CombatBreakdownDiagnostics(CombatSimulator simulator, Player player) {
        this.simulator = simulator;
        this.player = player;
    }

    DamageBreakdown simulateMeleeAttack(String skillId) {
        LivingEntity target = simulator.getDummyEntity(skillId);
        if (target == null || !target.isValid()) return null;
        DamageBreakdown.startTracking(player);
        target.setNoDamageTicks(0);
        player.attack(target);
        DamageBreakdown result = DamageBreakdown.stopTracking(player);
        if (result != null) result.compute();
        return result;
    }

    String simulateMultipleAttacks(String skillId, int hitCount) {
        LivingEntity target = simulator.getDummyEntity(skillId);
        if (target == null || !target.isValid()) return "§cNo valid target found!";
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
        if (eliteEntity == null) return "§cTarget is not an elite entity!";

        StringBuilder report = new StringBuilder();
        report.append("§6=== COMBAT SIMULATION REPORT ===\n");
        report.append(String.format("§7Target: §f%s §7(Lv %d, HP: %.0f)\n",
                target.getName(), eliteEntity.getLevel(), target.getMaxHealth()));
        report.append(String.format("§7Hits simulated: §f%d\n\n", hitCount));

        double totalDamage = 0;
        double minDamage = Double.MAX_VALUE;
        double maxDamage = 0;
        int completedHits = 0;
        int crits = 0;
        DamageBreakdown sampleBreakdown = null;
        for (int index = 0; index < hitCount; index++) {
            if (!target.isValid() || target.isDead() || target.getHealth() <= 0) {
                simulator.healDummy(skillId);
                target = simulator.getDummyEntity(skillId);
                if (target == null) break;
            }
            DamageBreakdown breakdown = simulateMeleeAttack(skillId);
            if (breakdown != null) {
                double damage = breakdown.getFinalDamage();
                totalDamage += damage;
                minDamage = Math.min(minDamage, damage);
                maxDamage = Math.max(maxDamage, damage);
                completedHits++;
                if (breakdown.isCriticalHit()) crits++;
                if (sampleBreakdown == null && !breakdown.isCriticalHit()) sampleBreakdown = breakdown;
            }
            simulator.healDummy(skillId);
        }

        double averageDamage = completedHits > 0 ? totalDamage / completedHits : 0;
        double critRate = completedHits > 0 ? (double) crits / completedHits * 100 : 0;
        double hitsToKill = averageDamage > 0 ? target.getMaxHealth() / averageDamage : 0;
        report.append("§6--- DAMAGE STATISTICS ---\n");
        report.append(String.format("§7Average Damage: §f%.1f\n", averageDamage));
        report.append(String.format("§7Min/Max: §f%.1f §7/ §f%.1f\n", completedHits > 0 ? minDamage : 0, maxDamage));
        report.append(String.format("§7Crit Rate: §f%.1f%% §7(%d/%d)\n", critRate, crits, completedHits));
        report.append(String.format("§7Estimated Hits to Kill: §f%.1f\n", hitsToKill));
        report.append(String.format("§7Total DPS (1 hit/s): §f%.1f\n\n", averageDamage));
        if (sampleBreakdown != null) {
            report.append("§6--- SAMPLE HIT BREAKDOWN ---\n");
            report.append(sampleBreakdown.toFormattedString());
        }
        return report.toString();
    }

    void sendToPlayer(DamageBreakdown breakdown) {
        if (breakdown == null) {
            Logger.sendMessage(player, "§cNo damage breakdown available.");
            return;
        }
        for (String line : breakdown.toFormattedString().split("\n")) Logger.sendMessage(player, line);
    }

    void runQuickDamageTest(int dummyLevel) {
        String dummyConfig = "training_dummy_lv" + dummyLevel + ".yml";
        CustomBossesConfigFields config = CustomBossesConfig.getCustomBoss(dummyConfig);
        if (config == null) {
            Logger.sendMessage(player, "§cDummy config not found: " + dummyConfig);
            return;
        }
        org.bukkit.util.Vector direction = player.getLocation().getDirection();
        direction.setY(0).normalize();
        Location spawnLocation = player.getLocation().add(direction.multiply(5));
        int highestY = spawnLocation.getWorld().getHighestBlockYAt(spawnLocation);
        spawnLocation.setY(highestY + 1);
        spawnLocation.setDirection(player.getLocation().toVector().subtract(spawnLocation.toVector()).normalize());

        CustomBossEntity dummy = RegionalBossEntity.createTemporaryRegionalBossEntity(dummyConfig, spawnLocation);
        if (dummy == null) {
            Logger.sendMessage(player, "§cFailed to spawn dummy!");
            return;
        }
        dummy.spawn(true);
        if (dummy.getLivingEntity() == null) {
            Logger.sendMessage(player, "§cDummy entity is null!");
            return;
        }
        LivingEntity entity = dummy.getLivingEntity();
        entity.setMaximumNoDamageTicks(0);
        entity.setNoDamageTicks(0);
        entity.setCustomName("§eDamage Test Dummy §7(Lv " + dummyLevel + ")");
        entity.setCustomNameVisible(true);
        entity.setGlowing(true);
        Logger.sendMessage(player, "§aSpawned Lv" + dummyLevel + " test dummy. Attack it to see damage breakdown!");
        Logger.sendMessage(player, "§7Dummy HP: §f" + String.format("%.0f", entity.getMaxHealth()));
        DamageBreakdown.startTracking(player);
    }

    boolean isTracking() {
        return DamageBreakdown.isTracking(player);
    }

    DamageBreakdown stopTracking() {
        return DamageBreakdown.stopTracking(player);
    }
}
