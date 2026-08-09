package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tier 1 ARMOR skill - Evasion
 * Chance to completely evade incoming attacks
 */
public class EvasionSkill extends SkillBonus implements ProcSkill {

    /**
     * Dodge chance, on the shared defensive power budget.
     * <p>
     * Sustained power is {@code E = uptime * reduction} and the budget is 0.20. A dodge is a full
     * negation, so its reduction term is 1.0 and the whole budget goes into the proc rate:
     * {@code 0.20 / 1.0}. The old formula scaled from 10% to a 35% cap, i.e. up to 0.35 sustained
     * power, nearly double budget at high levels. Hardcoded on purpose: balance values no longer
     * come from config, only presentation does.
     */
    private static final double EVASION_CHANCE = 0.20;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public EvasionSkill() {
        super(
            SkillType.ARMOR,
            10,
            "Evasion",
            "Chance to completely dodge incoming attacks",
            SkillBonusType.PROC,
            1,
            "armor_evasion"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        // No persistent bonus to remove
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Returns 1.0 for full damage negation when evasion procs
        // The proc chance is handled separately via getProcChance()
        return 1.0;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }

    // ProcSkill interface methods

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        // Flat 20%: a dodge negates the whole hit, so the proc rate is the entire power budget
        return EVASION_CHANCE;
    }

    @Override
    public void onProc(Player player, Object context) {
        // Visual effect for dodge
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.5f);
    }

    /**
     * Attempts to evade an incoming attack.
     * Called from damage event handler.
     *
     * @param player The player being attacked
     * @param context The PlayerDamagedByEliteMobEvent
     * @return true if the attack was evaded
     */
    public boolean tryEvade(Player player, Object context) {
        int skillLevel = getPlayerSkillLevel(player);
        if (ThreadLocalRandom.current().nextDouble() < getProcChance(skillLevel)) {
            onProc(player, context);
            return true;
        }
        return false;
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
